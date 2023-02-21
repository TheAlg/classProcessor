set SOURCE="C:\Users\a890420\Projects\Source\org\model"
set TARGET="C:\Users\a890420\Projects\Target\org\model"
set OPTIONS=-s %SOURCE%   -t %TARGET%
echo %OPTIONS%
java -cp target\classProcessor-1.0-SNAPSHOT.jar org.tools.ClassProcessor %OPTIONS%