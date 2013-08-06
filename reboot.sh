    # enable crond
    # crond calls getpwnam (user database search)
    PATH=$PATH:/data/busybox
    mount -o remount,rw -t yaffs2 `grep /system /proc/mounts | cut -d' ' -f1` /system
    echo "root:x:0:0::/data/cron:/system/bin/bash" > /etc/passwd
    mount -o remount,ro -t yaffs2 `grep /system /proc/mounts | cut -d' ' -f1` /system
    # crond has "/bin/sh" hardcoded
    mount -o remount,rw rootfs /
    ln -s /system/bin/ /bin
    mount -o remount,ro rootfs /
    # set timezone
    TZ=EET-10EETDT
    export TZ
    # use /data/cron, call the crontab file "root"
    crond -c /data/cron
