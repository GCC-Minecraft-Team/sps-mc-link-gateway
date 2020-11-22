IF NOT EXIST TestMC02_end\yatopia.jar (
    mkdir TestMC02_end
    cd TestMC02_end
    curl -o yatopia.jar -L https://api.yatopia.net/v2/build/11/download?branch=ver/1.16.4

    echo eula=true>eula.txt
    cd ..
)

cd TestMC02_end
mkdir plugins
cd ..

cd TestMC02_end
java -Xms4g -Xmx4g -XX:ActiveProcessorCount=8 -XX:+UseG1GC -jar yatopia.jar nogui
pause
