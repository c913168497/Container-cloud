#!/bin/bash
#=========================================
#   SYSTEM REQUIRED:  Linux Centos6
#   DESCRIPTION:  kubectl delete deploy
#   AUTHOR: Panjie
#   Email:  panjie@szzt.com.cn
#=========================================
kube_master_ip=$1
port=$2
namespace=$3
deploy_name=$4

scp -P $port /opt/appcloud/resources/delete_deploy.sh root@$kube_master_ip:/opt/

ssh -l root $kube_master_ip -p $port "
/opt/delete_deploy.sh $namespace $deploy_name
"
