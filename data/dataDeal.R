
shop_2 <- read.csv("E:/tianchibigdata/last mile delivery/part 2/new_3.csv",head=F,col.names = c("shop_id","lon","lan"))
spot_2 <- read.csv("E:/tianchibigdata/last mile delivery/part 2/new_2.csv",head=F,col.names = c("spot_id","lon","lan"))
site_2 <- read.csv("E:/tianchibigdata/last mile delivery/part 2/new_1.csv",head=F,col.names = c("site_id","lon","lan"))
branch_2 <- read.csv("E:/tianchibigdata/last mile delivery/part 2/new_4.csv",head=F,col.names = c("order_id","spot_id","site_id","num"))
o2o_2 <- read.csv("E:/tianchibigdata/last mile delivery/part 2/new_5.csv",head=F,col.names = c("order_id","spot_id","shop_id","pickup_time","delivery_time","num"))

#时间转换
difftime(strptime("08:00","%H:%M"),strptime("08:01","%H:%M"),units = "mins")
o2o_2$pickup_time <- difftime(strptime(o2o_2$pickup_time,"%H:%M"),strptime("08:00","%H:%M"),units = "mins")
o2o_2$delivery_time <- difftime(strptime(o2o_2$delivery_time,"%H:%M"),strptime("08:00","%H:%M"),units = "mins")

transforFun <- function(){
  data <- data.table()
  for(i in 1:nrow(site_2))
  {
    x <- data.table(site_id=site_2[i,]$site_id,spot_id=site_2[i,]$site_id,num=0,lon=site_2[i,]$lon,lan=site_2[i,]$lan,order_id="null")
    temp <- merge(data.table(branch_2),data.table(spot_2),by=c("spot_id"),all.x = T)
    y <- temp[temp$site_id==site_2[i,]$site_id,]
    z <- rbind(x,y)
    data <- rbind(data,z)
  }
  return(data)
}

#o2o订单处理
m <- merge(data.table(o2o_2),data.table(spot_2),by=c("spot_id"),all.x=T)
n <- shop_2
colnames(n) <- c("shop_id","shop_lon","shop_lan")
mn <- merge(m,data.table(n),by=c("shop_id"),all.x=T)
mn2 <- mn
mn$time <- round(2*6378137*asin(sqrt((sin(pi/180*(mn$lan-mn$shop_lan)/2))^2+cos(pi/180*mn$lan)*cos(pi/180*mn$shop_lan)*(sin(pi/180*(mn$lon-mn$shop_lon)/2))^2))/250)
mn$stayTime <- round(3*sqrt(mn$num)+5)


deal_data <- function(file1,file2,file3,file4,file5)
{
  shop_2 <- read.csv(file3,head=F,col.names = c("shop_id","lon","lan"))
  spot_2 <- read.csv(file2,head=F,col.names = c("spot_id","lon","lan"))
  site_2 <- read.csv(file1,head=F,col.names = c("site_id","lon","lan"))
  branch_2 <- read.csv(file4,head=F,col.names = c("order_id","spot_id","site_id","num"))
  o2o_2 <- read.csv(file5,head=F,col.names = c("order_id","spot_id","shop_id","pickup_time","delivery_time","num"))
  
  
  branch_data <- transforFun()
  
  m <- merge(data.table(o2o_2),data.table(spot_2),by=c("spot_id"),all.x=T)
  n <- shop_2
  colnames(n) <- c("shop_id","shop_lon","shop_lan")
  mn <- merge(m,data.table(n),by=c("shop_id"),all.x=T)
  mn2 <- mn
  mn$time <- round(2*6378137*asin(sqrt((sin(pi/180*(mn$lan-mn$shop_lan)/2))^2+cos(pi/180*mn$lan)*cos(pi/180*mn$shop_lan)*(sin(pi/180*(mn$lon-mn$shop_lon)/2))^2))/250)
  mn$stayTime <- round(3*sqrt(mn$num)+5)
  
  o2o_data <- mn
  
  write.csv(spot_2,"E:/tianchibigdata/last mile delivery/part 2/spot.csv",row.names = F)
  write.csv(site_2,"E:/tianchibigdata/last mile delivery/part 2/site.csv",row.names = F)
  write.csv(shop_2,"E:/tianchibigdata/last mile delivery/part 2/shop.csv",row.names = F)
  write.csv(branch_data,"E:/tianchibigdata/last mile delivery/part 2/branch_data.csv",row.names = F)
  write.csv(o2o_data,"E:/tianchibigdata/last mile delivery/part 2/o2o_data.csv",row.names = F)
  
}

#对结果进行优化-------------------------------------------------------------------------------------------------------------
dis <- function(x1,y1,x2,y2){
  round(2*6378137*asin(sqrt((sin(pi/180*(y1-y2)/2))^2+cos(pi/180*y1)*cos(pi/180*y2)*(sin(pi/180*(x1-x2)/2))^2))/250)
}

dis2 <- function(shop_name1,shop_name2){
  x1 <- shop_2[shop_2$shop_id==shop_name1,2]
  x2 <- shop_2[shop_2$shop_id==shop_name2,2]
  y1 <- shop_2[shop_2$shop_id==shop_name1,3]
  t2 <- shop_2[shop_2$shop_id==shop_name2,3]
  round(2*6378137*asin(sqrt((sin(pi/180*(y1-y2)/2))^2+cos(pi/180*y1)*cos(pi/180*y2)*(sin(pi/180*(x1-x2)/2))^2))/250)
}


getSiteDisMat <- function(site){
  mat <- matrix(nrow = nrow(site),ncol=nrow(site))
  for(i in 1:nrow(site))
  {
    for(j in 1:nrow(site))
    {
      mat[i,j] <- dis(site[i,2],site[i,3],site[j,2],site[j,3])
    }
  }
  return(mat)
}

siteCluster <- apply(mat, 1, getCluster)
getCluster <- function(data){
  a <- c()
  for(i in 1:length(data))
  {
    if(data[i] < 10)
    {
      a <- c(a,i)
    }
  }
  return(a)
}

resultTemp <- result[result$V1 >= "D0675",]

setCourierName <- function(data,names){
  j = 1
  for(i in 1:nrow(data))
  {
    if(data[i,9] == 0)
    {
      data[i,1] <- names[j]
    }
    if(data[i,9] > 0)
    {
      data[i,1] <- names[j]
      j <- j+1
    }
  }
  return(data)
}

getNewCourier <- function(data){
  m <- data$V1
  m <- as.data.frame(table(m))
  m <- m$m
  len <- length(m)
  m <- data.frame(V1=m,label=1:length(m))
  head(m)
  n <- merge(data.table(data),data.table(m),by="V1",all.x=T)
  n <- data.frame(n)
  
  newData <- data.frame()
  for(i in 1:len)
  {
    data_2 <- n[n$label==i,1:6]
    newData <- rbind(newData,seperateF(data_2))
  }
  return(newData)
}

seperateF <- function(data){
  data <- addLabel(data)
  data$V1 <- as.character(data$V1)
  #print(summary(data))
  newdata <- data.frame()
  #colnames(newdata) <- names(data)
  data2 <- data.frame()
  #colnames(data2) <- names(data)
  for(i in 1:nrow(data))
  {
    if(data[i,9] == 0)
    {
      data[i,1] <- names[x+1]
      newdata <- rbind(newdata,data[i,])
      if(i == nrow(data))
      {
        data2 <- rbind(data2,datareset(newdata))
        x <<- x+1
      }
    }
    if(data[i,9] > 0)
    {
      data[i,1] <- names[x+1]
      newdata <- rbind(newdata,data[i,])
      x <<- x+1
      #colnames(newdata) <- names(data)
      data2 <- rbind(data2,datareset(newdata))
      newdata <- data.frame()
      
    }
    
  }
  return(data2)
}

datareset <- function(data){
  a <- data[1,3]
  data$V3 <- data$V3 - a
  data$V4 <- data$V4 - a
  return(data)
}

addLabel <- function(data){
  data$V7 <- data$V4-data$V3
  v8 <- c()
  for(i in 1:nrow(data))
  {
    if(i < nrow(data) && substr(data[i,2],0,1) == "B" && substr(data[i+1,2],0,1) == "A")
      v8 <- c(v8,data[i+1,3]-data[i,4])
    else
      v8 <- c(v8,0)
  }
  data$V8 <- v8
  
  data$V9 <- apply(data,1,applyF)
  return(data)
}

applyF <- function(data){
  data <- as.vector(unlist(data))
  print(data)
  if(as.numeric(data[8]) == 0)
      return(0)
  else if(as.numeric(data[8])+as.numeric(data[7]) >= 39)
      return(as.numeric(data[8])+as.numeric(data[7]))
  else
    return(0)
}


#数据可视化----------------------------------------------------------------------------------------------------
site <- read.csv("F:\\ML\\last mile delivery/1.csv")#网点的分布
colnames(site) <- c("site_id","lon","lan")
ggmap(get_map(location ='Shanghai',zoom = 9,maptype='terrain'),extent='device')+
  geom_point(data=site,aes(x=lon,y=lan),colour='red',alpha=0.7,size=1) 

spot <- read.csv("F:\\ML\\last mile delivery/2.csv")#配送点的分布
colnames(spot) <- c("spot_id","lon","lan")
ggmap(get_map(location ='Shanghai',zoom = 9,maptype='terrain'),extent='device')+
  geom_point(data=spot,aes(x=lon,y=lan),colour='red',alpha=0.7,size=0.5)

shop <- read.csv("F:\\ML\\last mile delivery/part 2/new_3.csv")#商户点分布
colnames(shop) <- c("shop_id","lon","lan")
ggmap(get_map(location ='Shanghai',zoom = 9,maptype='terrain'),extent='device')+
  geom_point(data=shop,aes(x=lon,y=lan),colour='red',alpha=0.7,size=0.85)

branch_order <- read.csv("F:\\ML\\last mile delivery/4.csv") #电商订单
colnames(branch_order) <- c("order_id","spot_id","site_id","num")
o20_order <- read.csv("F:\\ML\\last mile delivery/5.csv") #o2o订单
colnames(o2O_order) <- c("order_id","spot_id","shop_id","pickup_time","delivery_time","num")
courier <- read.csv("F:\\ML\\last mile delivery/6.csv") # 快递员id



#数据可视化，每个网点以及其对应的配送点颜色不一样，且网点用星号，配送点圆圈，并思考
#是否可以根据网点的电商包裹大小，配送点包裹的大小绘制大小不同的点

x <- rnorm(1000) #不同数据不同颜色，124个网点则用 rainbow(1000)
cols <- rainbow(1000)
plot(x, col = cols)

plot_map <- function(data,cols){ #可以画图，进行修改，在电商订单中为每个网点分配一个id  1-124
  # 画网点与配送点的分布
  map <- ggmap(get_map(location ='Shanghai',zoom = 9,maptype='terrain'),extent='device')
  for(i in 1:124){
    data_ <- data[data[,7] == i,]
    a <- merge(data.table(data_[1,1:4]),data.table(site),by="site_id",all.x=T)
    map <- map+geom_point(data=a,aes(x=lon,y=lan),colour=cols[i],alpha=1,size=2,pch=15)
    map <- map+geom_point(data=data_,aes(x=lon,y=lan),colour=cols[i],alpha=0.5,size=0.85,pch=1)
  }
  map
}

plot_map2 <- function(data,cols){ 
  # 画商户点与配送点的分布
  map <- ggmap(get_map(location ='Shanghai',zoom = 9,maptype='terrain'),extent='device')
  for(i in 1:300){
    data_ <- data[data[,9] == i,]
    a <- merge(data.table(data_[1,1:6]),data.table(shop),by="shop_id",all.x=T)
    map <- map+geom_point(data=a,aes(x=lon,y=lan),colour=cols[i],alpha=1,size=1,pch=15)
    map <- map+geom_point(data=data_,aes(x=lon,y=lan),colour=cols[i],alpha=0.7,size=1,pch=20)
  }
  map
}

ggmap(get_map(location ='Shanghai',zoom = 9,maptype='terrain'),extent='device')+
  if(1==1)  geom_point(data=merchant_site,aes(x=lon,y=lan),colour='red',alpha=0.7,size=0.85)

plotfunc <- function(data,i) {
  map <- ggmap(get_map(location ='Shanghai',zoom = 9,maptype='terrain'),extent='device')
  data_ <- data[data[,7] == i,]
  a <- merge(data.table(data_[1,1:4]),data.table(site),by="site_id",all.x=T)
  map <- map+geom_point(data=a,aes(x=lon,y=lan),colour="blue",alpha=1,size=2,pch=15)
  map <- map+geom_point(data=data_,aes(x=lon,y=lan),colour="red",alpha=0.5,size=0.85,pch=1)
  map
}

plotfunc2 <- function(data,i) {
  map <- ggmap(get_map(location ='Shanghai',zoom = 9,maptype='terrain'),extent='device')
  data_ <- data[data[,9] == i,]
  a <- merge(data.table(data_[1,1:6]),data.table(shop),by="shop_id",all.x=T)
  map <- map+geom_point(data=a,aes(x=lon,y=lan),colour="blue",alpha=1,size=3,pch=15)
  map <- map+geom_point(data=data_,aes(x=lon,y=lan),colour="red",alpha=0.7,size=2,pch=20)
  map
}

saveGif(for( i in 1:124) print(plotfunc(as.data.frame(z),i))) #生成动画图片

saveVideo(for( i in 1:2) print(plotfunc(as.data.frame(z),i)),video.name = "site_spot.mp4",interval = 0.30)
saveVideo(for( i in 1:2) print(plotfunc2(as.data.frame(z2),i)),video.name = "shop_spot.mp4",interval = 2)