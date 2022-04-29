FROM golang:alpine

# 为我们的镜像设置必要的环境变量
ENV GO111MODULE=on \
    CGO_ENABLED=0 \
    GOOS=linux \
    GOARCH=amd64 \
	GOPROXY="https://goproxy.cn,direct"

# 移动到工作目录：/home/www/goWebBlog 这个目录 是你项目代码 放在linux上
# 这是我的代码跟目录
# 你们得修改成自己的
WORKDIR /obs

# 将代码复制到容器中
COPY . .

# 将我们的代码编译成二进制可执行文件  可执行文件名为 app
RUN go build -o /usr/local/sbin/dolphins /obs/src

RUN rm -rf /obs
WORKDIR /

# 启动容器时运行的命令
CMD ["/usr/local/sbin/dolphins", "-P", "1"]
