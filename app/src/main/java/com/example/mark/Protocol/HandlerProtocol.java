package com.example.mark.Protocol;

/**
 * Created by Mark on 2016/7/6.
 */
public interface HandlerProtocol
{
	//定義協議字串長度
	int PROTOCOL_LEN=4;

	String SPLIT_SIGN="|";
	String etContent_UPDATE="2200";
	String spUser_INSERT="2201";
	String spUser_DELETE="2202";
	String selfIP_UPDATE="1100";
	String give_INFORMATION="2000";
	String draw_Node="2010";
	String Start_SoundTrack="2001";
	String Stop_Rerord_Sound="2002";
	String success_Calculate_Coordinate="2004";
	String success_Calculate_Distance="2003";
}
