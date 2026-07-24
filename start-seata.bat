@echo off
set "SEATA_HOME=D:\StudySoftware2\seata"
set "PROJECT_CONFIG=D:\project\mall-cloud\config\seata\seata-server.yml"

cd /d "%SEATA_HOME%"
call "%SEATA_HOME%\bin\seata-server.bat" -p 8091 --spring.config.location="file:%PROJECT_CONFIG%"
