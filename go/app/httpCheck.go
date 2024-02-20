package app

import (
	"errors"
	"io/ioutil"
	"net/http"
	"net/url"
	"time"

	"github.com/gin-gonic/gin"
)

type requestHttpCheck struct {
	Url     string `json:"url"`     // 检查的url
	Timeout int    `json:"timeout"` // 检查的超时设置
}

type responseHttpCheck struct {
	CheckResCode int    `json:"check_res_code"` // 检查返回码，约定如果检查异常返回码为1，其他为http返回码如200
	CheckRes     string `json:"check_res"`      // 检查返回结果
	ErrMsg       string `json:"err_msg"`        // 调用错误概要信息
	Err          string `json:"err"`            // 调用错误详细输出信息
	Res          string `json:"res"`            // url请求的返回的body
}

// HttpCheck
// @Tags HttpCheck
// @Summary http检查
// @Description 用于http连接检查
// @Security jwt
// @param authorization header string false "Token"
// @Accept json
// @Produce json
// @Param requestHttpCheck body requestHttpCheck true "body"
// @Success 200 {object} responseHttpCheck "ok"
// @Router /check/http [post]
func HttpCheck(c *gin.Context) {
	var res responseHttpCheck
	// 获取参数
	param := requestHttpCheck{}
	if err := c.BindJSON(&param); err != nil {
		res.ErrMsg = "解析参数失败"
		res.Err = err.Error()
		c.AbortWithError(http.StatusInternalServerError, &gin.Error{
			Err:  err,
			Type: gin.ErrorTypePublic,
		})
		return
	}

	client := http.Client{
		Timeout: time.Duration(param.Timeout) * time.Second,
	}

	httpRes, err := client.Get(param.Url)
	if err != nil {
		handleError(c, err, res)
		return
	}

	body, err := ioutil.ReadAll(httpRes.Body)
	if err != nil {
		handleError(c, err, res)
		return
	}
	httpRes.Body.Close() // 确保关闭响应体

	res.CheckRes = httpRes.Status
	res.CheckResCode = httpRes.StatusCode
	res.Res = string(body)
	c.JSON(http.StatusOK, res)
}

func handleError(c *gin.Context, err error, res responseHttpCheck) {
	var urlErr *url.Error
	if errors.As(err, &urlErr) {
		res.CheckResCode = 1 // 约定如果检查异常， 返回码为1
		res.CheckRes = urlErr.Error()
	} else {
		res.ErrMsg = "请求失败"
		res.Err = err.Error()
	}
	c.AbortWithError(http.StatusInternalServerError, err)
}
