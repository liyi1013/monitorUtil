package auth

import (
	"github.com/gin-gonic/gin"
	"log"
	"net/http"
	"time"
)

func unauthorizedResponse(msg string) (int, gin.H) {
	return http.StatusUnauthorized, gin.H{
		"msg": msg,
		"err": "authorize fail.",
	}
}

// IDMap 存放调用方ip对应的id信息，如”1.2.3.4“:”1“
// 当调用方重新请求更新token时，更新id，并将id放入token中，使得前面的token失效
var IDMap map[string]string = make(map[string]string)

// GenerateJWTTokenWithIP 生成token
func GenerateJWTTokenWithIP(expireTimeHours int, reqIp string) (string, error) {
	issuer := "hzyfzc"
	audience := reqIp
	userId := time.Now().Format(time.StampMilli)
	subject := ""
	token, err := GenerateToken(GenerateKey(), expireTimeHours, issuer, audience, userId, subject)
	if err != nil {
		return "", err
	}
	IDMap[reqIp] = userId
	return token, nil
}

// CheckJWTWithIP 身份验证
func CheckJWTWithIP(JwtSigningKey []byte) gin.HandlerFunc {
	return func(c *gin.Context) {
		token := c.GetHeader("Authorization")
		if token == "" {
			log.Printf("Token is empty.")
			c.Abort() // 如果后面还有其他中间件函数，不执行
			c.JSON(unauthorizedResponse("Token is empty."))
			return // 跳出当前中间件函数
		}

		claim, err := ParseToken(token, JwtSigningKey)

		if err != nil {
			log.Printf("ParseToken error %s\n", err.Error())
			c.Abort()
			c.JSON(unauthorizedResponse("Incorrect token."))
			return
		} else {

			// 根据请求者的ip，和token中的ip对比验证。
			// 以免token被其他机器获取使用。一个token只能一个服务器使用。
			// 约定claim.Audience来存调用方的ip。
			reqIP := c.ClientIP()

			if reqIP != claim.Audience {
				log.Printf("Invalid token. reqIP=%s, not match ip in token\n", reqIP)
				c.Abort()
				c.JSON(unauthorizedResponse("Wrong caller"))
				return
			} else {
				// 根据请求者的token中的id对比IDMap验证。
				id, ok := IDMap[reqIP]
				if !ok {
					log.Printf("Invalid token. reqIP=%s, id %s not in IDMap\n", reqIP, id)
					c.Abort()
					c.JSON(unauthorizedResponse("Wrong caller"))
					return
				}
				if id != claim.Id {
					log.Printf("Invalid error.")
					c.Abort()
					c.JSON(unauthorizedResponse("Abandoned token"))
					return
				}
				c.Next() // 继续调用后面的中间件函数, 调用好后回到这边继续执行(入栈出栈)
			}
		}
	}
}
