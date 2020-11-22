IF NOT EXIST TestProxy\waterfall.jar (
    mkdir TestProxy
    cd TestProxy
    curl -o waterfall.jar https://papermc.io/ci/job/Waterfall/lastSuccessfulBuild/artifact/Waterfall-Proxy/bootstrap/target/Waterfall.jar

    echo eula=true>eula.txt
    cd ..
)

cd TestProxy
mkdir plugins
cd ..

copy target\sps-mc-link-gateway-1.0-SNAPSHOT.jar TestProxy\plugins\sps-mc-gateway-latest.jar

cd TestProxy
java -Xms4g -Xmx4g -XX:ActiveProcessorCount=8 -XX:+UseG1GC -jar waterfall.jar nogui
pause
