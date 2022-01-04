// 작성자: 김진희

package com.kjh.blescanner;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.os.ParcelUuid;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.kjh.blescanner.domain.BluetoothDeviceData;
import com.kjh.blescanner.service.BluetoothLeService;

import java.util.ArrayList;

public class BluetoothDeviceViewAdapter extends RecyclerView.Adapter<BluetoothDeviceViewAdapter.ViewHolder> {

    private MainActivity mainActivity;    
    private ArrayList<BluetoothDeviceData> itemList = null ; // 화면에 보여줄 디바이스 정보
    private ArrayList<BluetoothDevice> deviceList = null ;  // 내부적으로 기억할 디바이스 객체

    public void clear() {
        ArrayList<BluetoothDevice> deviceList = new ArrayList<>();
        this.deviceList = deviceList;

        // 리사이클러뷰에 표시할 데이터 리스트 생성.
        ArrayList<BluetoothDeviceData> itemList = new ArrayList<>();
        itemList.add(new BluetoothDeviceData("이름", "MAC", null));
        this.itemList = itemList;
    }

    // 아이템 뷰를 저장하는 뷰홀더 클래스.
    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvMAC, tvUUID;

        ViewHolder(View itemView) {
            super(itemView) ;

            // 뷰 객체에 대한 참조. (hold strong reference)
            tvName = itemView.findViewById(R.id.tvName);
            tvMAC = itemView.findViewById(R.id.tvMAC);
            tvUUID = itemView.findViewById(R.id.tvUUID);

            // 클릭하면 UUID를 보여줌
            itemView.setOnClickListener(listener->{
                String uuid = getUuid();
//                tvUUID.setText(uuid);
            });
        }

        // DeviceControlActivity 를 이용해서 서비스 UUID와 특성 UUID를 보여줌
        private String getUuid() {
            int pos = getAdapterPosition();
            if (pos > 0) {
                BluetoothDevice device = deviceList.get(pos-1);

                Toast.makeText(mainActivity, "선택한 디바이스 이름: " +
                                device.getName() + "\nMAC: " + device.getAddress(),
                        Toast.LENGTH_SHORT).show();

                // 디바이스 이름과 주소를 가지고서, DeviceControlActivity를 연다.
                final Intent intent = new Intent(mainActivity, DeviceControlActivity.class);
                intent.putExtra(DeviceControlActivity.EXTRAS_DEVICE_NAME, device.getName());
                intent.putExtra(DeviceControlActivity.EXTRAS_DEVICE_ADDRESS, device.getAddress());
                mainActivity.startActivity(intent);
            }
            return "";
        }
    }

    // 생성자에서 데이터 리스트 객체를 전달받음.
    BluetoothDeviceViewAdapter(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
        clear();
    }

    // 화면에 보여줄 디바이스 목록에 디바이스를 추가하고, 내부적으로 디바이스 정보도 저장한다.
    public boolean addDevice(BluetoothDevice device) {

        String name = device.getName();
        String mac = device.getAddress();
        String uuids[] = null;
        ParcelUuid parcelUuids[] = device.getUuids();

//        Log.d("parcelUuids", parcelUuids+"");

        // 표시할 UUID가 있는 경우 바로 표시함
        if (parcelUuids != null) {
            uuids = new String[parcelUuids.length];

            for (int i=0; i<parcelUuids.length; i++) {
                uuids[i] = parcelUuids[i].toString();
            }
        } else {
            uuids = new String[]{"클릭하면 연결하여 UUID 확인함"};
        }

        // 이름이 없거나 확인 안 된 디바이스는 화면에 표시할 목록에서 "이름없음"으로 표기함
        if (name == null || name.isEmpty()) {
            name = "이름없음";
        }

        // 화면에 보여줄 정보 객체를 만듬
        BluetoothDeviceData data = new BluetoothDeviceData(name, mac, uuids);
//        list.add(data);
//        Log.d("deviceList", list+"");
        
        // 추가할 디바이스가 이미 추가되어 있는지 MAC 주소로 확인함
        boolean duplicated = false;
        for (BluetoothDeviceData prevData : itemList) {
            if (data.getMac().equals(prevData.getMac())) {
                duplicated = true;
                break;
            }
        }

        // 중복되지 않은 경우만 디바이스를 목록에 추가하고 세부 정보를 기억함
        if (!duplicated) {
            // 화면에 보여줄 디바이스 목록 추가
            itemList.add(data);
            // 내부적으로 목록에 표시된 디바이스 정보 저장
            deviceList.add(device);
            Log.d("deviceList", itemList +"");
        }
        return !duplicated;
    }


    // onCreateViewHolder() - 아이템 뷰를 위한 뷰홀더 객체 생성하여 리턴.
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext() ;
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) ;

        View view = inflater.inflate(R.layout.device_item, parent, false) ;
        ViewHolder vh = new ViewHolder(view) ;

        return vh ;
    }

    // onBindViewHolder() - position에 해당하는 데이터를 뷰홀더의 아이템뷰에 표시.
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        BluetoothDeviceData bluetoothDeviceData = itemList.get(position);

        holder.tvName.setText(bluetoothDeviceData.getName());
        holder.tvMAC.setText(bluetoothDeviceData.getMac());
        if (bluetoothDeviceData.getUuids() != null) {
            holder.tvUUID.setText(bluetoothDeviceData.getUuids()[0]);
        }
    }

    // getItemCount() - 전체 데이터 갯수 리턴.
    @Override
    public int getItemCount() {
        return itemList.size();
    }
}