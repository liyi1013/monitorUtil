package app

import (
	"errors"
	"github.com/gin-gonic/gin"
	"io/ioutil"
	"log"
	"net/http"
	"net/url"
	"strings"
	"time"
)

type requestPostCheck struct {
	Url     string `json:"url"`     // 检查的url
	Timeout int    `json:"timeout"` // 检查的超时设置
	Body    string `json:"body"`    // 请求的body
	//User    string `json:"user"`    // 请求的用户
	//Passwd  string `json:"passwd"`  // 请求的用户的密码
	//Keyword string `json:"keyword"` // 返回应包含的关键字
}

//{
//"body": "{\"timeout\": 10,\"url\": \"http://www.baidu.com\"}",
//"keyword": "string",
//"passwd": "string",
//"timeout": 10,
//"url": "http://localhost:8080/check/http",
//"user": "string"
//}

type responsePostCheck struct {
	CheckResCode int    `json:"check_res_code"` // 检查返回码，约定如果检查异常返回码为1，其他为http返回码如200
	CheckRes     string `json:"check_res"`      // 检查返回结果
	ErrMsg       string `json:"err_msg"`        // 调用错误概要信息
	Err          string `json:"err"`            // 调用错误详细输出信息
	Res          string `json:"res"`            // url请求的返回
}

// PostCheck
// @Tags PostCheck
// @Summary post检查
// @Description 用于http连接检查
// @Security jwt
// @param authorization header string false "Token"
// @Accept json
// @Produce json
// @Param requestPostCheck body requestPostCheck true "body"
// @Success 200 {object} responsePostCheck "ok"
// @Router /check/post [post]
func PostCheck(c *gin.Context) {
	var res responsePostCheck
	// 获取参数
	param := requestPostCheck{}
	err := c.BindJSON(&param)
	if err != nil {
		res.ErrMsg = "解析参数失败"
		res.Err = err.Error()
		c.JSON(500, res)
		return
	}

	//values := map[string]string{"name": "John Doe", "occupation": "gardener"}
	//json_data, err := json.Marshal(values)

	client := http.Client{
		Timeout: time.Duration(param.Timeout) * time.Second,
	}
	payload := strings.NewReader(param.Body)
	//httpRes, err := client.Post(param.Url, "application/json", bytes.NewBuffer([]byte(param.Body)))
	httpRes, err := client.Post(param.Url, "application/json", payload)

	if err != nil {
		log.Printf("[ERROR][PostCheck][http.post] %s\n", err.Error())
		var urlErr *url.Error
		if errors.As(err, &urlErr) {
			res.CheckResCode = 1
			res.CheckRes = urlErr.Error()
		}
		c.JSON(200, res)
		return
	}
	body, err := ioutil.ReadAll(httpRes.Body)
	if err != nil {
		log.Printf("[ERROR][PostCheck][read res body] %s\n", err.Error())
		res.CheckResCode = 1 // 约定读取body异常， 返回码为1
		res.CheckRes = err.Error()
		return
	}
	res.CheckRes = httpRes.Status
	res.CheckResCode = httpRes.StatusCode
	res.Res = string(body)
	c.JSON(200, res)
}
