package com.lyx;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class InputStr
{
	public static InputStreamReader INPUT_STREAM_READER = new InputStreamReader(System.in);
	public static BufferedReader BUFFER_READER = new BufferedReader(INPUT_STREAM_READER);

	/**
	 * 从键盘输入字符串
	 */
	public static String getStrFromKeyboard() throws IOException
	{
		String str = BUFFER_READER.readLine();
		return str;
	}

	public static void close() throws IOException
	{
		BUFFER_READER.close();
		INPUT_STREAM_READER.close();
	}
}