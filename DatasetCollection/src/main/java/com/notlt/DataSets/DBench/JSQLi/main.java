package com.notlt.DataSets.DBench.JSQLi;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.notlt.DataSets.DBench.BugInfo;
import com.notlt.Utils.ExcelReader;
import com.notlt.Utils.FileUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.notlt.Utils.GithubFileDownloader.downloadFileFromGithub;


public class main {
    public static void downLoadFiles() {
        String filePath = "/home/xx/文档/BIT-DBench/Java/JSQLIs/JSQLIs.xlsx";
        String fileDownloadBashPath = "dataSet/JSQLIs/bugFile/";

        int success = 0;
        try (FileInputStream fis = new FileInputStream(filePath);
             XSSFWorkbook workbook = new XSSFWorkbook(fis)) {

            // 获取第一个工作表（Sheet）
            Sheet sheet = workbook.getSheetAt(0);


            // 遍历行
            for (Row row : sheet) {
                // 跳过标题行（通常第一行是标题，根据实际数据决定是否跳过）
                if (row.getRowNum() == 0) {
                    continue;
                }
                try {

                    String githubUrl = row.getCell(2).getStringCellValue();
                    System.out.println(githubUrl);
                    String file_with_nodefect = String.valueOf(row.getCell(10).getRichStringCellValue());
                    System.out.println(file_with_nodefect);

                    String oneJavaPushFile = file_with_nodefect.split(";")[0];
                    String oneJavaPushFileHash = oneJavaPushFile.split(":")[0];
                    String oneJavaPushFilePath = oneJavaPushFile.split(":")[1];

                    downloadFileFromGithub(githubUrl, oneJavaPushFilePath, fileDownloadBashPath + oneJavaPushFileHash);

                    success++;

                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                    throw new RuntimeException(ex);
                } catch (Exception e) {
                    System.out.println("error:" + e);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(success);
    }

    private static List<BugInfo> getBugInfoListSQL(String filePath, int index) {
        List<BugInfo> bugInfoList = new ArrayList<>();
        Sheet sheet = ExcelReader.getSheet(filePath, index);
        // 遍历行
        if (sheet != null) {
            for (Row row : sheet) {
                // 跳过标题行（通常第一行是标题，根据实际数据决定是否跳过）
                if (row.getRowNum() == 0) {
                    continue;
                }
                // 遍历单元格
                BugInfo bugInfo = new BugInfo();
                try {

                    bugInfo.setId(String.valueOf((int) row.getCell(0).getNumericCellValue()));

                    String javaFilePath = row.getCell(9).getStringCellValue().split(";")[0].split(":")[1];
                    File file = new File(javaFilePath);

                    // 使用File类的getName()方法获取文件名
                    String fileName = file.getName();
                    bugInfo.setClassName(fileName.split(".java")[0]);

                    bugInfo.setBugFileName(row.getCell(9).getStringCellValue().split(";")[0].split(":")[0]);
                    bugInfo.setFixFileName(row.getCell(10).getStringCellValue().split(";")[0].split(":")[0]);
                } catch (Exception e) {
                    System.out.println("error:" + e);
                }
                bugInfoList.add(bugInfo);
            }
        }

        return bugInfoList;
    }

    public static void main(String[] args) throws FileNotFoundException {
        downLoadFiles();


        List<BugInfo> bugInfoListSQL = getBugInfoListSQL("/home/xx/文档/BIT-DBench/Java/JSQLIs/JSQLIs.xlsx", 0);
        for (BugInfo bugInfo : bugInfoListSQL) {
            List<MethodDeclaration> methodsOld = new ArrayList<>();
            List<MethodDeclaration> methodsNew = new ArrayList<>();

            try {

                String oldFile = "/home/xx/文档/BIT-DBench/Java/JSQLIs/Files/" + bugInfo.getBugFileName() + ".java";
                CompilationUnit cu = StaticJavaParser.parse(new File(oldFile));
                // 遍历类中的所有方法
                for (TypeDeclaration<?> type : cu.getTypes()) {
                    List<MethodDeclaration> methods = type.getMethods();
                    methodsOld.addAll(methods);
                }

                String newFile = "dataSet/JSQLIs/bugFile/" + bugInfo.getFixFileName();
                CompilationUnit cu2 = StaticJavaParser.parse(new File(newFile));
                // 遍历类中的所有方法
                for (TypeDeclaration<?> type : cu2.getTypes()) {
                    List<MethodDeclaration> methods = type.getMethods();
                    methodsNew.addAll(methods);
                }
                //

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


//                System.out.println();
                    // 方法是已存在但内容有变
                    if (matchedMethod.isPresent()) {
                        String oldBody = matchedMethod.get().toString();
                        if (!oldBody.equals(newMethod.toString())) {
                            System.out.println("modify: " + newMethod.getName() + " " + newMethod.getParameters());
                            modifiedMethods.add(newMethod.toString());
                            bugMethods.add(matchedMethod.get().toString());

                            System.out.println(modifiedMethods);
                            System.out.println(bugMethods);
                        }
                    }
                }

                System.out.println(bugInfo.getId());

                if (bugMethods.size() == 1 && modifiedMethods.size() == 1) {
                    String targetFileName = bugInfo.getId() + "_" + bugInfo.getBugFileName() + "_" + bugInfo.getFixFileName() + ".java.txt";

                    String bugText = String.join("\n", bugMethods);
                    String fixText = String.join("\n", modifiedMethods);
                    FileUtil.writeStringToFile(bugText, "dataSet/DBench/JSQLi/bugMethod/" + targetFileName);
                    FileUtil.writeStringToFile(fixText, "dataSet/DBench/JSQLi/fixedMethod/" + targetFileName);

                }

            } catch (Exception e) {
                System.out.println("error" + e);
            }
        }
    }

    public static boolean isParametersEqual(MethodDeclaration methods, MethodDeclaration methodInNew) {
        if (methods.getParameters().size() != methodInNew.getParameters().size()) {
            return false;
        }
        NodeList<Parameter> parameters = methods.getParameters();
        NodeList<Parameter> parametersew = methodInNew.getParameters();
        for (int i = 0; i < parameters.size(); i++) {
            if (!parameters.get(i).equals(parametersew.get(i))) {
                return false;
            }
        }
        return true;
    }
}
