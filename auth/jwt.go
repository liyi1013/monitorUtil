package auth

import (
	"errors"
	jwt "github.com/dgrijalva/jwt-go"
	"log"
	"monitorUtil/util"
	"time"
)

// GenerateKey 每个agent产生不同的key，
// ! 以保证生成的key在其他agent上认证不通过(不可用)
func GenerateKey() []byte {
	ip, err := util.GetLocalIP()
	if err != nil {
		ip = ""
	}
	JwtSigningKey := []byte("SignAt" + ip)
	return JwtSigningKey
}

//var JwtSigningKey []byte = GenerateKey()

// MyClaims Claim是一些实体（通常指的用户）的状态和额外的元数据
// ! [2022年3月4日]
type MyClaims struct {
	jwt.StandardClaims
}

// GenerateToken 产生token
func GenerateToken(key []byte, expireTimeHours int, issuer string, audience string, userId string, subject string) (string, error) {

	// 如果失效时间expireTimeHours设置为0 ，则永远不过期
	var expiresAt int64 = 0

	if expireTimeHours != 0 {
		//设置token有效时间
		expireTime := time.Now().Add(time.Duration(expireTimeHours) * time.Hour)
		// ! 【2022年3月4日16:03】agent服务器时间改了， 会影响token的有效性
		// ! 不设置过期时间, agent保存token值,比较验证
		expiresAt = expireTime.Unix()
	}

	claims := MyClaims{
		StandardClaims: jwt.StandardClaims{
			Audience:  audience,          // 接收者
			ExpiresAt: expiresAt,         // 过期时间
			Id:        userId,            // id
			IssuedAt:  time.Now().Unix(), // 发行时间
			Issuer:    issuer,            // 发行者
			NotBefore: time.Now().Unix(), // 生效时间
			Subject:   subject,           // jwt所面向的用户
		},
	}

	log.Printf("%#v\n", claims)
	tokenClaims := jwt.NewWithClaims(jwt.SigningMethodHS256, claims)
	token, err := tokenClaims.SignedString(key)
	return token, err
}

// ParseToken 解析token
func ParseToken(tokenStr string, key []byte) (*MyClaims, error) {

	tokenClaims, err := jwt.ParseWithClaims(
		tokenStr,
		&MyClaims{},
		func(token *jwt.Token) (interface{}, error) {
			if token.Method != jwt.GetSigningMethod("HS256") {
				log.Printf("JWT sign method not match.")
				return key, errors.New("JWT sign method not match")
			}
			return key, nil
		})

	if err != nil {
		return nil, err
	}

	if claims, ok := tokenClaims.Claims.(*MyClaims); !ok {
		return nil, err
	} else {
		return claims, nil
	}
}
