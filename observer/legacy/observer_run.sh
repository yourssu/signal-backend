cd /home/ubuntu/signal-api
sudo ps -ef | grep '[p]ython.*observer\.py' | awk '{print $2}' | xargs sudo kill -9
nohup env PYTHONPATH=$(pwd)/script python3 script/observer.py > observer.out 2>&1 &