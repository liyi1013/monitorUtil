package com.notlt.DataSets.DBench.JXss;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.notlt.DataSets.DBench.BugInfo;
import com.notlt.Utils.ExcelReader;
import com.notlt.Utils.FileUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.notlt.Utils.JavaMethodExtractor.isParametersEqual;


public class main {
    private static List<BugInfo> getBugInfoListXSS(String filePath, int sheetIndex) {
        List<BugInfo> bugInfoList = new ArrayList<>();
        Sheet sheet = ExcelReader.getSheet(filePath, sheetIndex);

        // 遍历行
        if (sheet != null) {
            for (Row row : sheet) {
                // 跳过标题行
                if (row.getRowNum() == 0) {
                    continue;
                }
                // 遍历单元格
                BugInfo bugInfo = new BugInfo();
                try {

                    bugInfo.setId(String.valueOf((int) row.getCell(0).getNumericCellValue()));

                    String javaFilePath = row.getCell(10).getStringCellValue().split(";")[0].split(":")[1];
                    File file = new File(javaFilePath);

                    // 使用File类的getName()方法获取文件名
                    String fileName = file.getName();
                    bugInfo.setClassName(fileName.split(".java")[0]);

                    bugInfo.setBugFileName(row.getCell(10).getStringCellValue().split(";")[0].split(":")[0]);
                    bugInfo.setFixFileName(row.getCell(11).getStringCellValue().split(";")[0].split(":")[0]);
                } catch (Exception e) {
                    System.out.println("error:" + e);
                }
                bugInfoList.add(bugInfo);
            }
        }
        return bugInfoList;
    }

    public static void main(String[] args) {
        List<BugInfo> bugInfoListXSS = getBugInfoListXSS("/home/xx/文档/BIT-DBench/Java/JXSSs/JXSSs.xlsx", 0);

        for (BugInfo bugInfo : bugInfoListXSS) {
            System.out.println(bugInfo);

            List<MethodDeclaration> methodsOld = new ArrayList<>();
            List<MethodDeclaration> methodsNew = new ArrayList<>();


            try {

                String oldFile = "/home/xx/文档/BIT-DBench/Java/JXSSs/Files/" + bugInfo.getBugFileName() + ".java";
                CompilationUnit cu = StaticJavaParser.parse(new File(oldFile));
                // 遍历类中的所有方法
                for (TypeDeclaration<?> type : cu.getTypes()) {
                    List<MethodDeclaration> methods = type.getMethods();
                    methodsOld.addAll(methods);
                }

                String newFile = "/home/xx/文档/BIT-DBench/Java/JXSSs/Files/" + bugInfo.getFixFileName() + ".java";
                CompilationUnit cu2 = StaticJavaParser.parse(new File(newFile));
                // 遍历类中的所有方法
                for (TypeDeclaration<?> type : cu2.getTypes()) {
                    List<MethodDeclaration> methods = type.getMethods();
                    methodsNew.addAll(methods);
                }

                // 比较文件，获取修改
                List<String> bugMethods = new ArrayList<>();
                List<String> modifiedMethods = new ArrayList<>();

                for (MethodDeclaration newMethod : methodsNew) {
                    System.out.println("newMethod: " + newMethod.getName() + " " + newMethod.getParameters());

                    // 查找旧版本中是否存在相同签名的方法
                    Optional<MethodDeclaration> matchedMethod = methodsOld.stream()
                            .filter(
                                    oldMethod ->
                                            oldMethod.getName().equals(newMethod.getName()) &&
                                                    isParametersEqual(oldMethod, newMethod)
                            ).findFirst();

                    // 方法是已存在但内容有变
                    if (matchedMethod.isPresent()) {
                        String oldBody = matchedMethod.get().toString();
                        if (!oldBody.equals(newMethod.toString())) {
                            System.out.println("modify: " + newMethod.getName() + " " + newMethod.getParameters());
                            modifiedMethods.add(newMethod.toString());
                            bugMethods.add(matchedMethod.get().toString());
                        }
                    }
                }

                if (bugMethods.size() == 1 && modifiedMethods.size() == 1) {
                    String targetFileName = bugInfo.getId() + "_" + bugInfo.getBugFileName() + "_" + bugInfo.getFixFileName() + ".java.txt";
                    String bugText = String.join("\n", bugMethods);
                    String fixText = String.join("\n", modifiedMethods);
                    FileUtil.writeStringToFile(bugText, "dataSet/DBench/JXss/bugMethod/" + targetFileName);
                    FileUtil.writeStringToFile(fixText, "dataSet/DBench/JXss/fixedMethod/" + targetFileName);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
