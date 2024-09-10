PROD=app.melt:typing-ex/systemd/

upload:
	scp Makefile update-stat.sh typing_ex_roll_call.* ${PROD}

install:
	cp *.service *.timer /lib/systemd/system
	systemctl daemon-reload

start:
#	systemctl start typing_ex_normal.timer
	systemctl start typing_ex_roll_call.timer

enable:
#	systemctl enable typing_ex_normal.timer
	systemctl enable typing_ex_roll_call.timer

stop:
#	systemctl stop typing_ex_normal.timer
	systemctl stop typing_ex_roll_call.timer

disable: stop
#	systemctl disable typing_ex_normal.timer
	systemctl disable typing_ex_roll_call.timer

uninstall: disable
	${RM} /lib/systemd/system/typing_ex_normal.*
	${RM} /lib/systemd/system/typing_ex_roll_call.*
