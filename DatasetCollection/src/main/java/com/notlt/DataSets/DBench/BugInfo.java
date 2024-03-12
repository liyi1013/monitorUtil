package com.notlt.DataSets.DBench;

public class BugInfo {
    public String id;
    public String bugFileName;
    public String fixFileName;
    public String className;
    public String methodName;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getBugFileName() {
        return bugFileName;
    }

    public void setBugFileName(String bugFileName) {
        this.bugFileName = bugFileName;
    }

    public String getFixFileName() {
        return fixFileName;
    }

    public void setFixFileName(String fixFileName) {
        this.fixFileName = fixFileName;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    @Override
    public String toString() {
        return "BugInfo{" +
                "id='" + id + '\'' +
                ", bugFileName='" + bugFileName + '\'' +
                ", fixFileName='" + fixFileName + '\'' +
                ", className='" + className + '\'' +
                ", methodName='" + methodName + '\'' +
                '}';
    }
}
