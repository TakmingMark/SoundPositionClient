package com.example.mark.Protocol;

public interface SocketProtocol
{
	//定義協議字串長度
	int PROTOCOL_LEN=2;

	//定義協議溝通內容
	String SPLIT_SIGN="|";  //切割字串用的，當需要切割時，有些特殊符號要加上\\
	String C_USER_ROUND="@#"; //使用者第一次登入Server給的訊息
	String S_NAME_REP="-1";  //回應使用者登入失敗
	String S_LOGIN_SUCCESS="1"; //回應使用者登入成功

	String C_PRIVATE_ROUND="#$"; //某使用者想私訊某使用者
	String S_PRIVATE_ROUND="#$";

	String S_USER_LOGIN="%^"; //當使用者登入進來，發送給其他使用者說此使用者登入做記錄
	String S_USER_LOGOUT="^&"; //當使用者登出時，Server發送給其他使用者說此使用者以登出

	String C_HEART_BEAT="&*"; //做心跳溝通用
	String S_HEART_BEAT="&*"; //做心跳溝通用

	String C_FIND_ASSISTANT_AND_TRANSMIT="*("; //公開做廣播，傳至每個人，有人開始要發射訊息
	String S_GIVEN_ROOM_AND_NO="(*";
	String S_RETURN_NODE_POSITION="%$";
	String S_USER_START_SOUND_TRACK="##"; //使用者開始輪流發射聲音訊號
	String C_USER_END_SOUND_TRACK="$$"; //使用者已經發射聲音訊號完畢

	String C_USER_START_SOUND_RECORD="^^";
	String S_USER_STOP_SOUND_RECORD="%%"; //使用者停止錄製聲音

	String C_FILE_UPLOAD_COMPLETED="@@"; //當檔案傳輸完畢的時候，回傳給伺服器說結束了

	String S_SUCCESS_CALCULATE_COORDINATE="@!";

	String S_SUCCESS_CALCULATE_DISTANCE="!!"; //當成功計算出距離時，回傳給client的訊息

	String C_INSERT_TARGET_NODE_DB="@%";

	String C_MSG_ROUND="!@";
	String S_MSG_ROUND="!@"; //其他訊息的回應
}