@echo off
title Smart Porter - Railway Porter Booking System
echo ==================================================================
echo   SMART PORTER - Luggage Management System
echo   Starting server on http://localhost:8080/
echo ==================================================================
echo.

cd /d "%~dp0"

REM Check if compiled classes exist in backend/bin
if not exist "backend\bin\com\smartporter\ServerRunner.class" (
    echo [INFO] Compiled classes not found. Compiling backend...
    if not exist "backend\bin" mkdir backend\bin

    javac -d backend\bin -cp "backend\lib\*" ^
      backend\src\main\java\com\smartporter\config\DotenvLoader.java ^
      backend\src\main\java\com\smartporter\config\AppConfig.java ^
      backend\src\main\java\com\smartporter\util\DBConnection.java ^
      backend\src\main\java\com\smartporter\util\PasswordUtil.java ^
      backend\src\main\java\com\smartporter\util\JsonUtil.java ^
      backend\src\main\java\com\smartporter\util\ResponseUtil.java ^
      backend\src\main\java\com\smartporter\exception\SmartPorterException.java ^
      backend\src\main\java\com\smartporter\exception\ValidationException.java ^
      backend\src\main\java\com\smartporter\model\Admin.java ^
      backend\src\main\java\com\smartporter\model\Passenger.java ^
      backend\src\main\java\com\smartporter\model\Porter.java ^
      backend\src\main\java\com\smartporter\model\Station.java ^
      backend\src\main\java\com\smartporter\model\StationLocation.java ^
      backend\src\main\java\com\smartporter\model\Booking.java ^
      backend\src\main\java\com\smartporter\model\Luggage.java ^
      backend\src\main\java\com\smartporter\model\Payment.java ^
      backend\src\main\java\com\smartporter\model\Feedback.java ^
      backend\src\main\java\com\smartporter\model\PricingRule.java ^
      backend\src\main\java\com\smartporter\dao\PassengerDAO.java ^
      backend\src\main\java\com\smartporter\dao\PorterDAO.java ^
      backend\src\main\java\com\smartporter\dao\StationDAO.java ^
      backend\src\main\java\com\smartporter\dao\BookingDAO.java ^
      backend\src\main\java\com\smartporter\dao\PaymentDAO.java ^
      backend\src\main\java\com\smartporter\dao\FeedbackDAO.java ^
      backend\src\main\java\com\smartporter\dao\AdminDAO.java ^
      backend\src\main\java\com\smartporter\dao\PricingRuleDAO.java ^
      backend\src\main\java\com\smartporter\service\AuthService.java ^
      backend\src\main\java\com\smartporter\service\BookingService.java ^
      backend\src\main\java\com\smartporter\service\PorterService.java ^
      backend\src\main\java\com\smartporter\service\FareService.java ^
      backend\src\main\java\com\smartporter\service\AdminService.java ^
      backend\src\main\java\com\smartporter\filter\CorsFilter.java ^
      backend\src\main\java\com\smartporter\filter\AuthenticationFilter.java ^
      backend\src\main\java\com\smartporter\controller\AuthServlet.java ^
      backend\src\main\java\com\smartporter\controller\StationServlet.java ^
      backend\src\main\java\com\smartporter\controller\PassengerServlet.java ^
      backend\src\main\java\com\smartporter\controller\BookingServlet.java ^
      backend\src\main\java\com\smartporter\controller\PorterServlet.java ^
      backend\src\main\java\com\smartporter\controller\PaymentServlet.java ^
      backend\src\main\java\com\smartporter\controller\FeedbackServlet.java ^
      backend\src\main\java\com\smartporter\controller\AdminServlet.java ^
      backend\src\main\java\com\smartporter\ServerRunner.java

    if %ERRORLEVEL% NEQ 0 (
        echo.
        echo [ERROR] Compilation failed! Check that JDK is installed.
        pause
        exit /b 1
    )
    echo [OK] Compilation successful.
)

echo.
echo [OK] Starting Smart Porter HTTP Server...
echo [OK] Open your browser at: http://localhost:8080/
echo [OK] Or directly:          http://localhost:8080/frontend/index.html
echo.
echo     Passenger login : http://localhost:8080/frontend/auth/login.html
echo     Porter login    : http://localhost:8080/frontend/auth/porter-login.html
echo     Admin login     : http://localhost:8080/frontend/auth/admin-login.html
echo.
echo Press Ctrl+C to stop the server.
echo ==================================================================
echo.

java -cp "backend\bin;backend\lib\*" com.smartporter.ServerRunner

pause
