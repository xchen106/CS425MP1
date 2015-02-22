#bin/bash
osascript -e 'tell app "Terminal"
do script "cd ~/Desktop && java -jar mp1.jar 'A' "
end tell'
osascript -e 'tell app "Terminal"
do script "cd ~/Desktop && java -jar mp1.jar 'B' "
end tell'
osascript -e 'tell app "Terminal"
do script "cd ~/Desktop && java -jar mp1.jar 'C' "
end tell'
osascript -e 'tell app "Terminal"
do script "cd ~/Desktop && java -jar mp1.jar 'D' "
end tell'
osascript -e 'tell app "Terminal"
do script "cd ~/Desktop && java -jar mp1.jar 'O' "
end tell'