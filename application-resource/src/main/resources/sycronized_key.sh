ip=$1
user_name=$2
password=$3
port=$4


echo "=====同步密钥开始====="
if [ `grep $ip /root/.ssh/known_hosts | wc -l` != 0 ];then

  sed -i "/$ip/d" /root/.ssh/known_hosts

fi

#打通ssh
/bin/sh /root/scripts/initialize_ssh.sh $ip $password $port 1>/opt/logs/initialize/$ipssh.log 2>/opt/logs/initialize/$ipssh.log

if [ $?. != 0. ];then
  vssh=`cat /opt/logs/initialize/$ipssh.log | grep password | wc -l`
  if [ $vssh. = 0. ];then
  echo "error=当前机器不允许用户名密码登录，请修改配置并重启ssh服务。"
  exit 1
  else
  echo "error=密码不正确!"
  exit 1
  fi
fi;
echo "=====密钥同步完成====="
