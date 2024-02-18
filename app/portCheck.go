package app

import (
	"errors"
	"fmt"
	"net"
	"time"

	"github.com/gin-gonic/gin"
)

type requestPortCheck struct {
	Ip      string `json:"ip"`      // 检查的ip
	Port    string `json:"port"`    // 检查的端口
	Timeout int    `json:"timeout"` // 检查的超时
}

type responsePortCheck struct {
	CheckResCode int    `json:"check_res_code"` // 检查返回码
	CheckRes     string `json:"check_res"`      // 检查返回结果
	ErrMsg       string `json:"err_msg"`        // 调用错误概要信息
	Err          string `json:"err"`            // 调用错误详细输出信息
}

// PortCheck
// @Tags PortCheck
// @Summary port检查
// @Description 用于port连接检查
// @Security jwt
// @param authorization header string false "Token"
// @Accept json
// @Produce json
// @Param requestPortCheck body requestPortCheck true "body"
// @Success 200 {object} responsePortCheck "ok"
// @Router /check/port [post]
func PortCheck(c *gin.Context) {
	var res responsePortCheck
	// 获取参数
	param := requestPortCheck{}
	err := c.BindJSON(&param)
	if err != nil {
		res.ErrMsg = "解析参数失败"
		res.Err = err.Error()
		c.JSON(500, res)
		return
	}

	conn, err := net.DialTimeout(
		"tcp",
		fmt.Sprintf("%s:%s", param.Ip, param.Port),
		time.Duration(param.Timeout)*time.Second)
	if err != nil {
		fmt.Printf("%#v\n", err)
		var netErr *net.OpError
		if errors.As(err, &netErr) {
			res.CheckResCode = 1
			res.CheckRes = netErr.Error()
		}
	} else {
		fmt.Printf("%#v\n", conn)
		res.CheckRes = "pong"
		res.CheckResCode = 0
	}
	c.JSON(200, res)
}
