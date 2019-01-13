#!/bin/bash

ip=$1
PASSWD=$2
port=$3
remotelog=/root/.ssh/authorized_keys

expect -c "
        set timeout 30;
        spawn ssh-copy-id -i /root/.ssh/id_rsa.pub $ip
        expect {
                \"(yes/no)?\" { send \"yes\r\";exp_continue;}
		\"Permission*\" { exit 145 }
                \"password:\" { send \"$PASSWD\r\";exp_continue;}
        }
	;
        "

