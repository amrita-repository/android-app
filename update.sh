git add .
echo "Enter commit message"
read msg
git commit -m "$msg"
git push origin master
