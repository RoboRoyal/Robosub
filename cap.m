Start = 3090; %when signal starts
Durration = 500; %how long signal lasts
freq = 35000; %freq of signal in Hz
X_loc = 10; %pos of sub, meters
Y_loc = 10; 
dir = 0; %direction sub is facing
dir = dir*pi/180;
sep = 1; %distance between hydrophones
sample_rate = 100000; %per
speed = 1498; %of sound in water
volume = 1000;
do_dist_vol = 0;
do_inter = 1;
simple = 127;
%731!!!!!! remember seven three one
%also 122
%-------------------------%
%fft_num = 
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

found_left = 0;
found_right = 0;
for index = 1:10000
    left_tmp = left_data(index:index+simple);
    right_tmp = right_data(index:index+simple);
    left_fft = fft(left_tmp);
    right_fft = fft(right_tmp);
    if left_fft(122) > 20 && found_left == 0
        disp('left: ');
        disp(index);
        left_time = index;
        found_left = 1;
    end
    if right_fft(122) > 20 && found_right == 0
        disp('right: ');
        disp(index);
        right_time = index;
        found_right = 1;
    end
end
%best_fft = fft(left_data(731:(731+simple)));
diff = 1498*(left_time-right_time)/100000;
new_dir = atan(sqrt(1-diff^2)/diff);
disp('dir: ');
disp(new_dir*180/pi);
plot(y,(left_data),'-','Linewidth',2,'color','blue');hold on;
axis([-.05 1 -1 1])
plot(y,(right_data),'-','Linewidth',2,'color','red');