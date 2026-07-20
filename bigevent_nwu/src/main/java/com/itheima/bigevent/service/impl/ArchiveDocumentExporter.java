package com.itheima.bigevent.service.impl;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.poi.xwpf.usermodel.*;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.*;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.*;

@Component
public class ArchiveDocumentExporter {
    private static final String WORD_CONTENT_TYPE =
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
    private static final String PDF_CONTENT_TYPE = "application/pdf";

    public Map<String, Object> export(Map<String, Object> workbench, String requestedFormat) {
        String format = Objects.toString(requestedFormat, "docx").trim().toLowerCase(Locale.ROOT);
        if (!Set.of("docx", "pdf").contains(format)) {
            throw new IllegalArgumentException("导出格式仅支持 docx 或 pdf");
        }
        Map<String, Object> archive = map(workbench.get("archive"));
        if (archive == null) throw new IllegalArgumentException("保护修复档案不存在");
        String baseName = safeFileName(text(archive.get("archiveCode")) + "-"
            + text(archive.get("archiveTitle")));
        byte[] data = "pdf".equals(format) ? createPdf(workbench) : createDocx(workbench);
        return Map.of(
            "fileName", baseName + "." + format,
            "contentType", "pdf".equals(format) ? PDF_CONTENT_TYPE : WORD_CONTENT_TYPE,
            "fileData", data
        );
    }

    private byte[] createDocx(Map<String, Object> workbench) {
        try (XWPFDocument document = new XWPFDocument();
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            configurePage(document);
            Map<String, Object> project = map(workbench.get("project"));
            Map<String, Object> archive = map(workbench.get("archive"));
            List<Section> sections = sections(workbench);

            XWPFParagraph title = document.createParagraph();
            title.setAlignment(ParagraphAlignment.CENTER);
            title.setSpacingBefore(1800);
            run(title, text(archive.get("archiveTitle")), 24, true);
            XWPFParagraph subtitle = document.createParagraph();
            subtitle.setAlignment(ParagraphAlignment.CENTER);
            run(subtitle, "文物保护修复档案", 16, false);

            XWPFTable cover = document.createTable(6, 2);
            cover.setWidth("85%");
            setCell(cover.getRow(0).getCell(0), "档案编号", true);
            setCell(cover.getRow(0).getCell(1), text(archive.get("archiveCode")), false);
            setCell(cover.getRow(1).getCell(0), "项目编号", true);
            setCell(cover.getRow(1).getCell(1), text(project.get("projectCode")), false);
            setCell(cover.getRow(2).getCell(0), "文物", true);
            setCell(cover.getRow(2).getCell(1), join(project.get("artifactCode"), project.get("artifactName")), false);
            setCell(cover.getRow(3).getCell(0), "编制人", true);
            setCell(cover.getRow(3).getCell(1), text(archive.get("compiler")), false);
            setCell(cover.getRow(4).getCell(0), "档案版本", true);
            setCell(cover.getRow(4).getCell(1), text(archive.get("currentVersion")), false);
            setCell(cover.getRow(5).getCell(0), "导出日期", true);
            setCell(cover.getRow(5).getCell(1), LocalDate.now().toString(), false);

            XWPFParagraph pageBreak = document.createParagraph();
            pageBreak.setPageBreak(true);
            heading(document, "目录", 1);
            for (int index = 0; index < sections.size(); index++) {
                paragraph(document, String.format("%02d  %s", index + 1, sections.get(index).title()));
            }

            for (int index = 0; index < sections.size(); index++) {
                Section section = sections.get(index);
                heading(document, String.format("%02d  %s", index + 1, section.title()), 1);
                for (Entry entry : section.entries()) {
                    if (entry.label().isBlank()) {
                        paragraph(document, entry.value());
                    } else {
                        keyValue(document, entry.label(), entry.value());
                    }
                }
            }

            XWPFParagraph footer = document.createParagraph();
            footer.setAlignment(ParagraphAlignment.CENTER);
            footer.setSpacingBefore(500);
            run(footer, "—— 档案导出结束 ——", 10, false);
            document.write(output);
            return output.toByteArray();
        } catch (Exception exception) {
            throw new IllegalStateException("Word档案生成失败", exception);
        }
    }

    private byte[] createPdf(Map<String, Object> workbench) {
        try (PDDocument document = new PDDocument();
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            PDType0Font font = loadChineseFont(document);
            Map<String, Object> archive = map(workbench.get("archive"));
            Map<String, Object> project = map(workbench.get("project"));
            try (PdfWriter writer = new PdfWriter(document, font)) {
                writer.center(text(archive.get("archiveTitle")), 20);
                writer.center("文物保护修复档案", 14);
                writer.blank(8);
                writer.line("档案编号：" + value(archive.get("archiveCode")), 11);
                writer.line("项目编号：" + value(project.get("projectCode")), 11);
                writer.line("文物：" + join(project.get("artifactCode"), project.get("artifactName")), 11);
                writer.line("编制人：" + value(archive.get("compiler")), 11);
                writer.line("档案版本：" + value(archive.get("currentVersion")), 11);
                writer.line("导出日期：" + LocalDate.now(), 11);
                writer.blank(12);

                List<Section> sections = sections(workbench);
                for (int index = 0; index < sections.size(); index++) {
                    Section section = sections.get(index);
                    writer.heading(String.format("%02d  %s", index + 1, section.title()));
                    for (Entry entry : section.entries()) {
                        String line = entry.label().isBlank()
                            ? entry.value() : entry.label() + "：" + entry.value();
                        writer.line(line, 10.5f);
                    }
                    writer.blank(5);
                }
            }
            document.getDocumentInformation().setTitle(text(archive.get("archiveTitle")));
            document.getDocumentInformation().setAuthor(text(archive.get("compiler")));
            document.save(output);
            return output.toByteArray();
        } catch (Exception exception) {
            throw new IllegalStateException("PDF档案生成失败，请确认系统已安装中文字体", exception);
        }
    }

    private List<Section> sections(Map<String, Object> workbench) {
        Map<String, Object> project = map(workbench.get("project"));
        Map<String, Object> archive = map(workbench.get("archive"));
        Map<String, Object> workspace = map(workbench.get("workspace"));
        List<Section> result = new ArrayList<>();

        result.add(section("项目与文物信息",
            entry("项目编号", project.get("projectCode")), entry("项目名称", project.get("projectName")),
            entry("文物编号", project.get("artifactCode")), entry("文物名称", project.get("artifactName")),
            entry("材质", project.get("material")), entry("所属墓葬", project.get("tombCode")),
            entry("负责人", project.get("principal")), entry("执行部门", project.get("department")),
            entry("项目类型", project.get("projectType")), entry("项目摘要", project.get("summary")),
            entry("档案摘要", archive.get("executiveSummary"))));

        Map<String, Object> survey = map(workspace.get("survey"));
        List<Entry> diseaseEntries = new ArrayList<>(List.of(
            entry("调查编号", survey.get("surveyCode")), entry("调查日期", survey.get("surveyDate")),
            entry("调查人", survey.get("surveyor")), entry("调查地点", survey.get("surveyLocation")),
            entry("总体保存状态", survey.get("preservationStatus")),
            entry("结构稳定性", survey.get("structuralStability")),
            entry("综合风险", survey.get("overallRiskLevel")), entry("调查总结", survey.get("summary"))
        ));
        int diseaseIndex = 1;
        for (Map<String, Object> item : list(workspace.get("diseaseRecords"))) {
            diseaseEntries.add(new Entry("病害 " + diseaseIndex++, joinParts(
                item.get("diseaseName"), item.get("diseaseCategory"), item.get("severity"),
                item.get("partName"), item.get("side"), item.get("positionDescription"),
                item.get("morphology"), item.get("causeAnalysis"), item.get("recommendedAction")
            )));
        }
        result.add(new Section("保存现状与病害调查", diseaseEntries));

        result.add(listSection("检测分析依据", workspace.get("detectionReferences"), item -> joinParts(
            item.get("experimentName"), item.get("experimentType"), item.get("method"),
            item.get("instrumentModel"), item.get("detectionDate"), item.get("conclusion"),
            item.get("relatedDisease"), item.get("purpose"), item.get("reportName")
        )));

        Map<String, Object> principles = map(workspace.get("principles"));
        Map<String, Object> goals = map(workspace.get("goals"));
        result.add(section("保护修复原则与目标",
            entry("保护原则", joinList(principles.get("selected"))),
            entry("原则说明", principles.get("custom")), entry("总体目标", goals.get("overall")),
            entry("病害控制目标", goals.get("diseaseControl")),
            entry("结构稳定目标", goals.get("structuralStability")),
            entry("外观目标", goals.get("appearance")),
            entry("信息保留目标", goals.get("informationRetention")),
            entry("长期保存目标", goals.get("longTerm"))));

        Map<String, Object> plan = map(workspace.get("plan"));
        List<Entry> planEntries = new ArrayList<>(List.of(
            entry("方案编号", plan.get("planCode")), entry("方案名称", plan.get("planName")),
            entry("方案状态", planStatus(plan.get("planStatus"))), entry("方案目标", plan.get("planGoal")),
            entry("技术依据", plan.get("technicalBasis")), entry("选用方法", plan.get("selectedMethod")),
            entry("预期结果", plan.get("expectedResult")), entry("风险分析", plan.get("riskAnalysis")),
            entry("安全要求", plan.get("safetyRequirements")),
            entry("环境要求", plan.get("environmentRequirements")),
            entry("可逆性说明", plan.get("reversibilityDescription")),
            entry("兼容性说明", plan.get("compatibilityDescription")),
            entry("应急措施", plan.get("emergencyMeasures"))
        ));
        int measureIndex = 1;
        for (Map<String, Object> item : list(workspace.get("planDiseaseList"))) {
            planEntries.add(new Entry("措施 " + measureIndex++, joinParts(
                item.get("diseaseName"), item.get("targetPart"), item.get("treatmentMethod"),
                item.get("operationSteps"), item.get("expectedResult")
            )));
        }
        result.add(new Section("保护修复方案", planEntries));

        List<Entry> materialEntries = new ArrayList<>();
        int materialIndex = 1;
        for (Map<String, Object> item : list(workspace.get("planMaterials"))) {
            materialEntries.add(new Entry("材料 " + materialIndex++, joinParts(
                item.get("materialName"), item.get("materialType"), item.get("manufacturer"),
                item.get("specification"), item.get("usagePurpose"), item.get("safetyNote"),
                item.get("compatibilityNote")
            )));
        }
        int toolIndex = 1;
        for (Map<String, Object> item : list(workspace.get("tools"))) {
            materialEntries.add(new Entry("工具 " + toolIndex++, joinParts(
                item.get("toolName"), item.get("toolType"), item.get("model"), item.get("purpose")
            )));
        }
        Map<String, Object> parameters = map(workspace.get("processParameters"));
        parameters.forEach((key, value) -> {
            if (!text(value).isBlank()) materialEntries.add(new Entry(parameterLabel(key), text(value)));
        });
        result.add(new Section("材料、工具与工艺参数", materialEntries));

        Map<String, Object> process = map(workspace.get("processSummary"));
        List<Entry> processEntries = new ArrayList<>(List.of(
            entry("计划步骤", process.get("planned")), entry("已完成步骤", process.get("completed")),
            entry("执行中步骤", process.get("processing")), entry("待执行步骤", process.get("pending"))
        ));
        int stepIndex = 1;
        for (Map<String, Object> item : list(process.get("steps"))) {
            processEntries.add(new Entry("步骤 " + stepIndex++, joinParts(
                item.get("stepCode"), item.get("stepName"), item.get("stepStatus"),
                item.get("operatorName"), item.get("targetPart"), item.get("completionRate")
            )));
        }
        result.add(new Section("修复过程记录", processEntries));

        result.add(listSection("修复前后对比", workspace.get("comparisons"), item -> joinParts(
            item.get("comparisonCode"), item.get("comparisonTitle"), item.get("targetPart"),
            item.get("beforeSummary"), item.get("afterSummary"), item.get("overallEffect"),
            item.get("remainingIssue")
        )));
        result.add(listSection("文物复原成果", workspace.get("restorationResults"), item -> joinParts(
            item.get("resultCode"), item.get("resultName"), item.get("restorationType"),
            item.get("targetPart"), item.get("resultSummary"), item.get("confidenceLevel"),
            item.get("finalConclusion")
        )));

        Map<String, Object> evaluation = map(workspace.get("evaluation"));
        result.add(section("效果评估与档案结论",
            entry("病害控制效果", evaluation.get("diseaseControl")),
            entry("结构变化", evaluation.get("structuralChange")),
            entry("表面强度", evaluation.get("surfaceStrength")),
            entry("外观协调性", evaluation.get("appearanceCoordination")),
            entry("目标达成情况", evaluation.get("goalAchievement")),
            entry("遗留问题", evaluation.get("remainingIssues")),
            entry("评价结论", evaluation.get("acceptanceConclusion")),
            entry("评价人", evaluation.get("evaluator")),
            entry("评价日期", evaluation.get("evaluationDate")),
            entry("档案最终结论", archive.get("finalConclusion"))));

        Map<String, Object> advice = map(workspace.get("advice"));
        result.add(section("保存环境与后续建议",
            entry("温度范围", advice.get("temperatureRange")), entry("湿度范围", advice.get("humidityRange")),
            entry("照明要求", advice.get("lighting")), entry("空气质量", advice.get("airQuality")),
            entry("包装要求", advice.get("packaging")), entry("搬运要求", advice.get("handling")),
            entry("防震要求", advice.get("shockproof")), entry("复查周期", advice.get("reviewCycle")),
            entry("监测病害", advice.get("monitorDiseases")),
            entry("监测指标", advice.get("monitoringIndicators")),
            entry("后续保护建议", advice.get("followUpAdvice")),
            entry("预警条件", advice.get("warningConditions"))));

        result.add(listSection("影像与附件清单", workspace.get("attachments"), item -> joinParts(
            item.get("fileName"), item.get("fileType"), item.get("sourceModule"),
            item.get("sectionName"), item.get("version"), item.get("description"),
            readableSize(item.get("fileSize"))
        )));
        return result;
    }

    private void configurePage(XWPFDocument document) {
        CTSectPr section = document.getDocument().getBody().isSetSectPr()
            ? document.getDocument().getBody().getSectPr()
            : document.getDocument().getBody().addNewSectPr();
        CTPageMar margins = section.isSetPgMar() ? section.getPgMar() : section.addNewPgMar();
        margins.setTop(BigInteger.valueOf(1134));
        margins.setBottom(BigInteger.valueOf(1134));
        margins.setLeft(BigInteger.valueOf(1276));
        margins.setRight(BigInteger.valueOf(1276));
    }

    private void heading(XWPFDocument document, String text, int level) {
        XWPFParagraph paragraph = document.createParagraph();
        paragraph.setStyle("Heading" + level);
        paragraph.setSpacingBefore(240);
        paragraph.setSpacingAfter(100);
        run(paragraph, text, level == 1 ? 16 : 13, true);
    }

    private void keyValue(XWPFDocument document, String label, String value) {
        XWPFParagraph paragraph = document.createParagraph();
        paragraph.setSpacingAfter(65);
        run(paragraph, label + "：", 10.5, true);
        run(paragraph, value(value), 10.5, false);
    }

    private void paragraph(XWPFDocument document, String value) {
        XWPFParagraph paragraph = document.createParagraph();
        paragraph.setSpacingAfter(65);
        run(paragraph, value(value), 10.5, false);
    }

    private XWPFRun run(XWPFParagraph paragraph, String text, double size, boolean bold) {
        XWPFRun run = paragraph.createRun();
        run.setText(value(text));
        run.setFontFamily("Microsoft YaHei");
        run.setFontSize(size);
        run.setBold(bold);
        return run;
    }

    private void setCell(XWPFTableCell cell, String value, boolean bold) {
        cell.removeParagraph(0);
        XWPFParagraph paragraph = cell.addParagraph();
        run(paragraph, value, 10.5, bold);
        cell.setVerticalAlignment(XWPFTableCell.XWPFVertAlign.CENTER);
    }

    private PDType0Font loadChineseFont(PDDocument document) throws Exception {
        List<Path> candidates = new ArrayList<>();
        String windir = System.getenv("WINDIR");
        if (windir != null) {
            candidates.add(Path.of(windir, "Fonts", "simhei.ttf"));
            candidates.add(Path.of(windir, "Fonts", "Deng.ttf"));
        }
        candidates.addAll(List.of(
            Path.of("C:/Windows/Fonts/simhei.ttf"),
            Path.of("C:/Windows/Fonts/Deng.ttf"),
            Path.of("/usr/share/fonts/opentype/noto/NotoSansCJK-Regular.ttf"),
            Path.of("/usr/share/fonts/truetype/wqy/wqy-zenhei.ttf"),
            Path.of("/System/Library/Fonts/PingFang.ttc")
        ));
        for (Path path : candidates) {
            if (Files.isRegularFile(path) && !path.toString().toLowerCase().endsWith(".ttc")) {
                return PDType0Font.load(document, path.toFile());
            }
        }
        throw new IllegalStateException("未找到可用于PDF导出的中文字体（建议安装黑体或Noto Sans CJK）");
    }

    private Section listSection(String title, Object source,
                                java.util.function.Function<Map<String, Object>, String> formatter) {
        List<Entry> entries = new ArrayList<>();
        int index = 1;
        for (Map<String, Object> item : list(source)) {
            entries.add(new Entry("记录 " + index++, formatter.apply(item)));
        }
        if (entries.isEmpty()) entries.add(new Entry("", "暂无记录"));
        return new Section(title, entries);
    }

    private Section section(String title, Entry... entries) {
        return new Section(title, new ArrayList<>(Arrays.asList(entries)));
    }

    private Entry entry(String label, Object value) {
        return new Entry(label, value(value));
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> map(Object value) {
        return value instanceof Map<?, ?> source ? (Map<String, Object>) source : new LinkedHashMap<>();
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> list(Object value) {
        return value instanceof List<?> source ? (List<Map<String, Object>>) source : List.of();
    }

    private String joinList(Object value) {
        if (!(value instanceof Collection<?> items)) return text(value);
        return items.stream().map(this::text).filter(item -> !item.isBlank())
            .reduce((left, right) -> left + "、" + right).orElse("");
    }

    private String join(Object... values) {
        return Arrays.stream(values).map(this::text).filter(item -> !item.isBlank())
            .reduce((left, right) -> left + " · " + right).orElse("-");
    }

    private String joinParts(Object... values) {
        return Arrays.stream(values).map(this::text).filter(item -> !item.isBlank())
            .reduce((left, right) -> left + "；" + right).orElse("暂无详细内容");
    }

    private String value(Object value) {
        String text = text(value);
        return text.isBlank() ? "—" : text;
    }

    private String text(Object value) {
        return Objects.toString(value, "").trim();
    }

    private String planStatus(Object value) {
        return "completed".equals(text(value)) ? "已完成" : "草稿";
    }

    private String readableSize(Object value) {
        if (!(value instanceof Number number)) return "";
        long size = number.longValue();
        if (size >= 1024 * 1024) return String.format(Locale.ROOT, "%.1f MB", size / 1024d / 1024d);
        if (size >= 1024) return String.format(Locale.ROOT, "%.1f KB", size / 1024d);
        return size + " B";
    }

    private String parameterLabel(String key) {
        return Map.ofEntries(
            Map.entry("operationMethod", "操作方法"), Map.entry("applicationOrder", "操作顺序"),
            Map.entry("materialConcentration", "材料浓度"), Map.entry("dryingTime", "干燥时间"),
            Map.entry("operationTimes", "操作次数"), Map.entry("temperature", "温度"),
            Map.entry("humidity", "湿度"), Map.entry("parameterLimit", "参数限制"),
            Map.entry("qualityControl", "质量控制"), Map.entry("emergencyRequirement", "应急要求")
        ).getOrDefault(key, key);
    }

    private String safeFileName(String value) {
        String result = value.replaceAll("[\\\\/:*?\"<>|\\r\\n]+", "_").trim();
        return result.isBlank() ? "保护修复档案" : result;
    }

    private record Entry(String label, String value) {}
    private record Section(String title, List<Entry> entries) {}

    private static final class PdfWriter implements AutoCloseable {
        private static final float MARGIN = 54;
        private static final float WIDTH = PDRectangle.A4.getWidth() - MARGIN * 2;
        private final PDDocument document;
        private final PDType0Font font;
        private PDPageContentStream stream;
        private float y;

        private PdfWriter(PDDocument document, PDType0Font font) throws Exception {
            this.document = document;
            this.font = font;
            newPage();
        }

        private void newPage() throws Exception {
            if (stream != null) stream.close();
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);
            stream = new PDPageContentStream(document, page);
            y = PDRectangle.A4.getHeight() - MARGIN;
        }

        private void ensure(float height) throws Exception {
            if (y - height < MARGIN) newPage();
        }

        private void center(String text, float size) throws Exception {
            ensure(size * 1.8f);
            float textWidth = font.getStringWidth(text) / 1000f * size;
            write(text, Math.max(MARGIN, (PDRectangle.A4.getWidth() - textWidth) / 2), y, size);
            y -= size * 1.8f;
        }

        private void heading(String text) throws Exception {
            ensure(32);
            y -= 5;
            write(text, MARGIN, y, 14);
            y -= 23;
        }

        private void line(String text, float size) throws Exception {
            for (String logicalLine : Objects.toString(text, "").split("\\R", -1)) {
                List<String> lines = wrap(logicalLine, size);
                if (lines.isEmpty()) lines = List.of("—");
                for (String value : lines) {
                    ensure(size * 1.55f);
                    write(value, MARGIN, y, size);
                    y -= size * 1.55f;
                }
            }
        }

        private List<String> wrap(String text, float size) throws Exception {
            List<String> lines = new ArrayList<>();
            StringBuilder current = new StringBuilder();
            for (int offset = 0; offset < text.length();) {
                int codePoint = text.codePointAt(offset);
                String character = new String(Character.toChars(codePoint));
                String candidate = current + character;
                if (!current.isEmpty() && font.getStringWidth(candidate) / 1000f * size > WIDTH) {
                    lines.add(current.toString());
                    current.setLength(0);
                }
                current.append(character);
                offset += Character.charCount(codePoint);
            }
            if (!current.isEmpty()) lines.add(current.toString());
            return lines;
        }

        private void write(String text, float x, float y, float size) throws Exception {
            stream.beginText();
            stream.setFont(font, size);
            stream.newLineAtOffset(x, y);
            stream.showText(text);
            stream.endText();
        }

        private void blank(float height) throws Exception {
            ensure(height);
            y -= height;
        }

        @Override
        public void close() throws Exception {
            if (stream != null) stream.close();
        }
    }
}
