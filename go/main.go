package main

import (
	"flag"
	"fmt"
	"log"
	"monitorUtil/app"
	"monitorUtil/auth"
	"net/http"

	"github.com/gin-gonic/gin"
)

// main is the entry point of the program.
func main() {
	// cliPort is the port number for the command-line interface.
	var cliPort = flag.String("p", "8080", "port")
	// printVersion is a boolean flag indicating whether to print the program version.
	var printVersion bool
	flag.BoolVar(&printVersion, "v", false, "print program version")
	flag.Parse()

	// If printVersion is true, print the program version and exit.
	if printVersion {
		log.Println("V20220705")
		log.Fatal("Program exited due to version request.")
	}

	// Create a new gin router.
	r := gin.Default()
	// Initialize Swagger.
	initSwagger()
	// Register routes to the router.
	registerRoutes(r)

	// Start the HTTP server.
	err := http.ListenAndServe(fmt.Sprintf(":%s", *cliPort), r)
	if err != nil {
		log.Fatal("Failed to start server:", err)
	}
}

// registerRoutes sets up the routes for the check API group.
func registerRoutes(r *gin.Engine) {
	apiRoute := r.Group("/check")
	apiRoute.Use(auth.CheckJWTWithIP(auth.GenerateKey()))

	apiRoute.POST("/http-check", app.HttpCheck) // 更明确的路由名称
	apiRoute.POST("/mysql-check", app.MysqlCheck)
	apiRoute.POST("/oracle-check", app.OracleCheck)
	apiRoute.POST("/port-check", app.PortCheck)
	apiRoute.POST("/post-request", app.PostCheck) // 更明确的路由名称
}
