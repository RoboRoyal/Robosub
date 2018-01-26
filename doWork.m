function data = doWork(left_data,right_data,do_search,num_pingers,simple,bucket)

for i=1:num_pingers
    max_pos(i)=i;
    max_val(i)=0;
end

found_left = 0;
found_right = 0;
for index = 1:195000
    left_tmp = left_data(index:index+simple);
    right_tmp = right_data(index:index+simple);
    left_fft = fft(left_tmp);
    right_fft = fft(right_tmp);
    if do_search == 1
        for i = 1:simple
            if left_fft(i)>min(max_val)
                [M,it]=min(max_val);
                max_pos(it)=i;
                max_val(it)=left_fft(i);
            end
        end
    end
    if do_search == 0 && left_fft(bucket) > 20 && found_left == 0
%         disp('left: ');
%         disp(index);
        left_time = index;
        found_left = 1;
    end
    if do_search == 0 && right_fft(bucket) > 20 && found_right == 0
%         disp('right: ');
%         disp(index);
        right_time = index;
        found_right = 1;
    end
end
if(do_search==1)
    data = max_pos;
end
if do_search == 0
    data(1) = left_time;
    data(2) = right_time;
end


%^^^^^
% if do_search == 1
%         for i = 1:simple
%             if left_fft(i)>max_val
%                 max_pos=i;
%                 max_val=left_fft(i);
%             end
%         end
%     end