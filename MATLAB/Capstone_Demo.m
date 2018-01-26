%Dakota Abernathy
Start = 3090; %when signal starts
Durration = 500; %how long signal lasts
freq = 25000; %freq of signal in Hz
X_loc = 10; %pos of sub, meters
Y_loc = -10; 
dir = 30; %direction sub is facing
dir =- dir*pi/180;
sep = 1; %distance between hydrophones
sample_rate = 100000; %per
speed = 1498; %of sound in water
volume = 1000;
do_dist_vol = 0;
do_inter = 1;
do_search = 0;
do_auto=1;
simple = 255;
grid_size = 20;
num_pingers =  1;
bucket = 5;%should equal find_bucket(freq,sample_rate,simple) 122,8
%search(raw)->[freq1,freq2...]
%function cap.m
%-------------------------%
left_d = sqrt(((sep/2)*sin(dir)+Y_loc)^2+((sep/2)*cos(dir)-X_loc)^2);
right_d = sqrt(((sep/2)*sin(dir)-Y_loc)^2+((sep/2)*cos(dir)+X_loc)^2);
left_t = (left_d / speed);
right_t = right_d / speed;
fun =@(x) rectpuls((x-Durration/(2*sample_rate) - Start/sample_rate),Durration/sample_rate) .* sin(freq.*x);
interfearence =@(x) do_inter .*(.6* sin(1234.*x) + .8* cos(38.*x) + .45.*sin(6500.*(x+.0123)));
y=linspace(0,2,sample_rate*2); %2 seconds at 100kHz
if do_dist_vol == 0
    left_d = right_d;
end
left_data = ((volume/left_d^2)*fun(y-left_t)+interfearence(y))/10;
right_data = ((volume/right_d^2)*fun(y-right_t)+interfearence(y))/10;

if do_auto==1
    data = doWork(left_data,right_data,1,num_pingers,simple,bucket);
    bucket=data;
    disp('Found bucket');
    data = doWork(left_data,right_data,0,num_pingers,simple,bucket);
    left_time = data(1);
    right_time = data(2);
    diff = 1498*(left_time-right_time)/100000;
new_dir = real(atan(sqrt(1-diff^2)/diff));
disp('Bucket was at: ');
disp(bucket);
disp('Calculated dir: ');
disp(new_dir*180/pi);
disp('Actual dir: ');
acc = ((atan(Y_loc/X_loc)*180/pi)+(180*dir/pi));
disp(acc);
plot(y,(left_data),'-','Linewidth',2,'color','blue');hold on;
axis([-.05 1 -1 1])
plot(y,(right_data),'-','Linewidth',2,'color','red');
figure
plot(0,0,'.');hold on;
X2 = X_loc-10*cos(new_dir);
Y2 = Y_loc-10*sin(new_dir);
acc = acc*pi/180;
X3 = X_loc-10*cos(acc);
Y3 = Y_loc-10*sin(acc);
if new_dir > 0
    X2 = X_loc+10*cos(new_dir);
    Y2 = Y_loc+10*sin(new_dir);
    X3 = X_loc+10*cos(acc);
    Y3 = Y_loc+10*sin(acc);
end
line([X_loc,X2], [Y_loc,Y2], 'Color', 'r', 'LineWidth', 2);hold on;
line([X_loc,0], [Y_loc,0], 'Color', 'b');
line([X_loc,X_loc+10*cos(-dir+pi/2)], [Y_loc,Y_loc+10*sin(-dir+pi/2)], 'Color', 'g');
axis([-grid_size grid_size -grid_size grid_size]);
axes('pos',[X_loc/(2*grid_size)+.5 Y_loc/(2*grid_size)+.5 .1 .1])
imshow('Sub.jpg')
end
