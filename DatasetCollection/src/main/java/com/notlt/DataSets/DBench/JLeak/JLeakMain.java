package com.notlt.DataSets.DBench.JLeak;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.notlt.DataSets.DBench.BugInfo;
import com.notlt.Utils.ExcelReader;
import com.notlt.Utils.FileUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import java.util.ArrayList;
import java.util.List;

import static com.notlt.Utils.JavaMethodExtractor.extractMethodList;

public class JLeakMain {

    public static List<BugInfo> getBugInfoList(String filePath, int sheetIndex) {
        List<BugInfo> bugInfoList = new ArrayList<>();
        Sheet sheet = ExcelReader.getSheet(filePath, sheetIndex);

        // 遍历行
        for (Row row : sheet) {
            // 跳过标题行（通常第一行是标题，根据实际数据决定是否跳过）
            if (row.getRowNum() == 0) {
                continue;
            }
            // 遍历单元格
            BugInfo bugInfo = new BugInfo();
            try {

                bugInfo.setId(String.valueOf((int) row.getCell(0).getNumericCellValue()));

                String fileNameAndMethod = row.getCell(17).getStringCellValue();
                String[] split = fileNameAndMethod.split(":");
                bugInfo.setClassName(split[0]);
                bugInfo.setMethodName(split[1]);
                bugInfo.setBugFileName(row.getCell(29).getStringCellValue());
                bugInfo.setFixFileName(row.getCell(30).getStringCellValue());
            } catch (Exception e) {
                System.out.println("error:" + e);
            }
            bugInfoList.add(bugInfo);
        }
        return bugInfoList;
    }

    // main方法为Jeak2023的工程
    public static void main(String[] args) throws Exception {
        String filePath = "/home/xx/文档/BIT-DBench/Java/JLeaks-2023/JLeaksDataset/JLeaks.xlsx";
        String baseSourcePath = "/home/xx/文档/BIT-DBench/Java/JLeaks-2023/JLeaksDataset/";
        String bugSourceFilePath = baseSourcePath + "all_bug_files/";
        String fixedSourceFilePath = baseSourcePath + "all_fix_files/";

        String fixedTargetJavaMethodPath = "dataSet/DBench/JLeak/fixed/";
        String bugTargetJavaMethodPath = "dataSet/DBench/JLeak/bug/";

        List<BugInfo> bugInfoList = getBugInfoList(filePath, 0);

        bugInfoList.stream().forEach(
                bugInfo ->
                {
                    String id = String.valueOf(bugInfo.getId());
                    String fixedJavaFileName = "fix-" + id + "-" + bugInfo.getFixFileName() + ".java";
                    String fixedSourceJavaFilePath = fixedSourceFilePath + fixedJavaFileName;
                    List<MethodDeclaration> methodDeclarations = extractMethodList(fixedSourceJavaFilePath, bugInfo.getMethodName());

                    String bugJavaFileName = "bug-" + id + "-" + bugInfo.getBugFileName() + ".java";
                    String bugSourceJavaFilePath = bugSourceFilePath + bugJavaFileName;
                    List<MethodDeclaration> methodDeclarationsBug = extractMethodList(bugSourceJavaFilePath, bugInfo.getMethodName());

                    if (methodDeclarations.size() == 1 && methodDeclarationsBug.size() == 1) {
                        String fixedJavaFileContent = methodDeclarations.get(0).toString();
                        String targetFileName = id + "-" + bugInfo.getBugFileName() + "-" + bugInfo.getFixFileName() + ".java.txt";
                        String fixedJavaMethodPath = fixedTargetJavaMethodPath + targetFileName;
                        FileUtil.writeStringToFile(fixedJavaFileContent, fixedJavaMethodPath);

                        //
                        String bugJavaFileContent = methodDeclarationsBug.get(0).toString();
                        String bugJavaMethodPath = bugTargetJavaMethodPath + targetFileName;
                        FileUtil.writeStringToFile(bugJavaFileContent, bugJavaMethodPath);

                    } else {
                        System.out.println(bugInfo.getId() + " : method size():" + methodDeclarations.size());
                    }
                }
        );
    }
}

