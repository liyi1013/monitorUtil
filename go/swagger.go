package main

import (
	"monitorUtil/docs"
)

// @title 作业平台
// @version 0.0.1
// @description 作业平台控制端
// @host localhost:8080
// @BasePath /
func initSwagger() {
	docs.SwaggerInfo.BasePath = "/"
	docs.SwaggerInfo.Title = "monitor util"
	docs.SwaggerInfo.Version = "V1.0.0"
	docs.SwaggerInfo.Description = "杭州研发支持 monitor util"
}
