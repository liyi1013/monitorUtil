package app

import (
	"database/sql"
	"errors"
	"fmt"
	"github.com/gin-gonic/gin"
	_ "github.com/sijms/go-ora/v2"
	go_ora "github.com/sijms/go-ora/v2"
	"github.com/sijms/go-ora/v2/network"
	"net"
)

type requestOracleCheck struct {
	Host    string `json:"host"`    // 检查的实例所在的ip
	Port    int    `json:"port"`    // 检查的实例所在的端口
	Db      string `json:"db"`      // 检查的实例名
	User    string `json:"user"`    // 检查的实例的用户
	Pwd     string `json:"pwd"`     // 检查的实例的密码
	Timeout int    `json:"timeout"` // 检查的超时设置
}

type responseOracleCheck struct {
	CheckResCode int    `json:"check_res_code"` // 检查返回码
	CheckRes     string `json:"check_res"`      // 检查返回结果
	ErrMsg       string `json:"err_msg"`        // 调用错误概要信息
	Err          string `json:"err"`            // 调用错误详细输出信息
}

// OracleCheck
// @Tags OracleCheck
// @Summary oracle检查
// @Description 用于oracle连通性检查
// @Security jwt
// @param authorization header string false "Token"
// @Accept json
// @Produce json
// @Param requestOracleCheck body requestOracleCheck true "body"
// @Success 200 {object} responseOracleCheck "ok"
// @Router /check/oracle [post]
func OracleCheck(c *gin.Context) {

	var res responseOracleCheck
	// 获取参数
	param := requestOracleCheck{}
	err := c.BindJSON(&param)
	if err != nil {
		res.ErrMsg = "解析参数失败"
		res.Err = err.Error()
		c.JSON(500, res)
		return
	}
	////"oracle://user:pass@server/service_name"
	//dsn := fmt.Sprintf("oracle://%s:%s@%s:%s/%s", param.User, param.Pwd, param.Host, param.Port, param.Db)
	//println(dsn)

	urlOptions := map[string]string{
		"CONNECTION TIMEOUT": fmt.Sprintf("%d", param.Timeout),
	}
	//databaseURL := go_ora.BuildUrl("localhost", 1521, "service", "user", "pass", nil)
	databaseURL := go_ora.BuildUrl(param.Host, param.Port, param.Db, param.User, param.Pwd, urlOptions)
	println(databaseURL)
	conn, err := sql.Open("oracle", databaseURL)
	// check for error
	defer conn.Close()

	if err != nil { // "连接数据库失败"
		res.CheckResCode = 1
		res.CheckRes = err.Error()
		c.JSON(200, res)
		return
	}
	connErr := conn.Ping()
	if connErr != nil {
		fmt.Printf("%#v\n", connErr)

		var netErr *net.OpError
		if errors.As(connErr, &netErr) {
			res.CheckResCode = 1
			res.CheckRes = netErr.Error()
		}

		var oracleErr *network.OracleError
		if errors.As(connErr, &oracleErr) {
			res.CheckResCode = oracleErr.ErrCode
			res.CheckRes = oracleErr.ErrMsg
		}

	} else {
		res.CheckResCode = 0
		res.CheckRes = "pong"
	}
	c.JSON(200, res)
}
