cd /home/ubuntu/signal-api
sudo ps -ef | grep '[p]ython.*observer\.py' | awk '{print $2}' | xargs sudo kill -9
nohup python3 observer.py > observer.out > /dev/null 2>&1 &