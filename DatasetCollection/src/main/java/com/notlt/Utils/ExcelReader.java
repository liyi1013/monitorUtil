package com.notlt.Utils;

import com.notlt.DataSets.ExcelReader123;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.IOException;

public class ExcelReader {
    public static Sheet getSheet(String filePath, int sheetIndex) {
        try (FileInputStream fis = new FileInputStream(filePath);
             XSSFWorkbook workbook = new XSSFWorkbook(fis)) {
            // 获取第工作表（Sheet）
            Sheet sheet = workbook.getSheetAt(sheetIndex);
            return sheet;
        }catch (IOException e){
            e.printStackTrace();
        }
        return null;
    }
}
