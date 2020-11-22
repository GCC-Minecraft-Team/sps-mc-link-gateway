IF NOT EXIST TestMC01_main\yatopia.jar (
    mkdir TestMC01_main
    cd TestMC01_main
    curl -o yatopia.jar -L https://api.yatopia.net/v2/build/11/download?branch=ver/1.16.4

    echo eula=true>eula.txt
    cd ..
)

cd TestMC01_main
mkdir plugins
cd ..

cd TestMC01_main
java -Xms4g -Xmx4g -XX:ActiveProcessorCount=8 -XX:+UseG1GC -jar yatopia.jar nogui
pause
