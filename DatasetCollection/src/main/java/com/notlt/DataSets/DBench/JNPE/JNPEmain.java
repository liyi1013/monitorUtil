package com.notlt.DataSets.DBench.JNPE;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.notlt.DataSets.DBench.BugInfo;
import com.notlt.Utils.ExcelReader;
import com.notlt.Utils.FileUtil;
import com.notlt.Utils.MethodNameExtractor;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import java.util.ArrayList;
import java.util.List;

import static com.notlt.Utils.JavaMethodExtractor.extractMethodList;

public class JNPEmain {

    public static List<BugInfo> getBugInfoListNPE(String filePath, int sheetIndex) {
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

                bugInfo.setId(String.valueOf(row.getCell(0).getStringCellValue()));

                String FullMethodName = row.getCell(9).getStringCellValue();
                int methodNum = (int) row.getCell(10).getNumericCellValue();
                if (methodNum > 1) {
                    System.out.println("methodNum > 1 . id: " + bugInfo.getId());
                    continue;
                }
                bugInfo.setMethodName(MethodNameExtractor.extractMethodName(FullMethodName));

                bugInfo.setClassName(row.getCell(6).getStringCellValue().split(":")[0].split(".java")[0]);
                bugInfo.setBugFileName(row.getCell(18).getStringCellValue().split(":")[0]);
                bugInfo.setFixFileName(row.getCell(17).getStringCellValue().split(":")[0]);
            } catch (Exception e) {
                System.out.println("error:" + e);
            }
            bugInfoList.add(bugInfo);
        }

        return bugInfoList;
    }

    public static void main(String[] args) throws Exception {
        String xlxsFilePath = "/home/xx/文档/BIT-DBench/Java/JNPEs/JNPEs.xlsx";
        String baseSourcePath = "/home/xx/文档/BIT-DBench/Java/JNPEs/Files/";

        String fixedTargetJavaMethodPath = "dataSet/DBench/JNPE/fixed/";
        String bugTargetJavaMethodPath = "dataSet/DBench/JNPE/bug/";

        List<BugInfo> bugInfoList = getBugInfoListNPE(xlxsFilePath, 0);

        bugInfoList.stream().forEach(
                bugInfo ->
                {
                    try{
                        String fixedJavaFileName = bugInfo.getFixFileName() + ".java";
                        String fixedSourceJavaFilePath = baseSourcePath + fixedJavaFileName;
                        List<MethodDeclaration> methodDeclarations = extractMethodList(fixedSourceJavaFilePath, bugInfo.getMethodName());

                        String bugJavaFileName = bugInfo.getBugFileName() + ".java";
                        String bugSourceJavaFilePath = baseSourcePath + bugJavaFileName;
                        List<MethodDeclaration> methodDeclarationsBug = extractMethodList(bugSourceJavaFilePath, bugInfo.getMethodName());

                        if (methodDeclarations.size() == 1 && methodDeclarationsBug.size() == 1) {
                            String fixedJavaFileContent = methodDeclarations.get(0).toString();
                            String targetFileName = bugInfo.getId() + "_" + bugInfo.getBugFileName() + "_" + bugInfo.getFixFileName() + ".java.txt";
                            String fixedJavaMethodPath = fixedTargetJavaMethodPath + targetFileName;
                            FileUtil.writeStringToFile(fixedJavaFileContent, fixedJavaMethodPath);

                            //
                            String bugJavaFileContent = methodDeclarationsBug.get(0).toString();
                            String bugJavaMethodPath = bugTargetJavaMethodPath + targetFileName;
                            FileUtil.writeStringToFile(bugJavaFileContent, bugJavaMethodPath);

                        } else {
                            System.out.println(bugInfo.getId() + " : method size():" + methodDeclarations.size());
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
        );
    }
}
