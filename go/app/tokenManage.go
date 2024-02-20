package app

import (
	"github.com/gin-gonic/gin"
	"monitorUtil/auth"
)

type requestToken struct {
	User            string `json:"user"`            // 用户
	Pwd             string `json:"pwd"`             // 密码
	ExpireTimeHours int    `json:"expireTimeHours"` // token超时时间

}

type responseToken struct {
	CheckResCode int    `json:"check_res_code"` // 检查返回码
	CheckRes     string `json:"check_res"`      // 检查返回结果
	ErrMsg       string `json:"err_msg"`        // 调用错误概要信息
	Err          string `json:"err"`            // 调用错误详细输出信息
}

// updateToken
// @Tags auth
// @Summary 更新token
// @Description 更新token
// @Accept json
// @Produce json
// @Param requestToken body requestToken true "body"
// @Success 200 {object} responseToken "ok"
// @Router /auth/updatetoken [post]
func UpdateToken(c *gin.Context) {
	var res responseToken
	// 获取参数
	param := requestToken{}
	err := c.BindJSON(&param)
	if err != nil {
		res.ErrMsg = "解析参数失败"
		res.Err = err.Error()
		c.JSON(500, res)
		return
	}

	if param.Pwd == "22119" && param.User == "hzyfzc" {
		token, err := auth.GenerateJWTTokenWithIP(param.ExpireTimeHours, c.ClientIP())
		if err != nil {
			return
		}
		res.CheckRes = token
		res.CheckResCode = 0
	} else {
		res.CheckResCode = 1
	}

	c.JSON(200, res)
}
