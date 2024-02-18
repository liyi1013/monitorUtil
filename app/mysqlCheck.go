package app

import (
	"database/sql"
	"errors"
	"fmt"
	"github.com/gin-gonic/gin"
	"github.com/go-sql-driver/mysql"
	_ "github.com/go-sql-driver/mysql"
	"net"
)

type requestMysqlCheck struct {
	Host    string `json:"host"`    // 检查的实例所在的ip
	Port    string `json:"port"`    // 检查的实例的端口
	Db      string `json:"db"`      // 检查的实例名
	User    string `json:"user"`    // 检查的实例的用户
	Pwd     string `json:"pwd"`     // 检查的实例的用户密码
	Timeout int    `json:"timeout"` // 检查的超时设置
}

type responseMysqlCheck struct {
	CheckResCode int    `json:"check_res_code"` // 检查返回码
	CheckRes     string `json:"check_res"`      // 检查返回结果
	ErrMsg       string `json:"err_msg"`        // 调用错误概要信息
	Err          string `json:"err"`            // 调用错误详细输出信息
}

// MysqlCheck
// @Tags MysqlCheck
// @Summary mysql检查
// @Description 用于mysql连通性检查
// @Security jwt
// @param authorization header string false "Token"
// @Accept json
// @Produce json
// @Param requestMysqlCheck body requestMysqlCheck true "body"
// @Success 200 {object} responseMysqlCheck "ok"
// @Router /check/mysql [post]
func MysqlCheck(c *gin.Context) {

	var res responseMysqlCheck
	// 获取参数
	param := requestMysqlCheck{}
	err := c.BindJSON(&param)
	if err != nil {
		res.ErrMsg = "解析参数失败"
		res.Err = err.Error()
		c.JSON(500, res)
		return
	}
	dsn := fmt.Sprintf("%s:%s@tcp(%s:%s)/%s?timeout=%ds", param.User, param.Pwd, param.Host, param.Port, param.Db, param.Timeout)
	println(dsn)
	db, err := sql.Open("mysql", dsn)
	defer db.Close()
	if err != nil { //"连接数据库失败"
		res.CheckResCode = 1
		res.CheckRes = err.Error()
		c.JSON(200, res)
		return
	}
	connErr := db.Ping()
	if connErr != nil {
		fmt.Printf("%#v\n", connErr)
		var mysqlErr *mysql.MySQLError
		if errors.As(connErr, &mysqlErr) {
			res.CheckResCode = int(mysqlErr.Number)
			res.CheckRes = mysqlErr.Message
		}
		var netErr *net.OpError
		if errors.As(connErr, &netErr) {
			res.CheckResCode = 1
			res.CheckRes = netErr.Error()
		}
	} else {
		res.CheckResCode = 0
		res.CheckRes = "pong"
	}
	c.JSON(200, res)
}
