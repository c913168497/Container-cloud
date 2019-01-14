package org.application.file.constants;

public class ResultCode {

    public enum ErrorCode {

        ERROR(3000, "系统异常,请联系管理员！"),
        PARMAS_ERROE(3001, "参数错误"),
        CAN_NOT_QUERY_VOLUME(3002, "无法查询到共享存储信息!");

        private int code;
        private String msg;

        ErrorCode(int code, String msg) {
            this.code = code;
            this.msg = msg;
        }

        public String getMsg() {
            return msg;
        }

        public int getCode() {
            return code;
        }
    }

}
