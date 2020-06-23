package com.lyx;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.file.FileReader;
import cn.hutool.core.io.file.FileWriter;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class Main
{
	public static void main(String[] args) throws IOException
	{
		System.out.println("选择操作：");
		System.out.println("输入1：排序 /Users/lyx/my-dir/software/cache/vscode工作区/笔记.code-workspace");
		System.out.println("输入2：手动输入路径");
		System.out.println("输入3：结束程序");

		while (true)
		{
			switch (InputStr.getStrFromKeyboard())
			{
				case "1":
				{
					sortWorkspaceFile("/Users/lyx/my-dir/software/cache/vscode工作区/笔记.code-workspace");
					InputStr.close();
					return;
				}
				case "2":
				{
					System.out.print("输入文件路径：");
					sortWorkspaceFile(InputStr.getStrFromKeyboard());
					InputStr.close();
					return;
				}
				case "3":
				{
					InputStr.close();
					return;
				}
				default:
				{
					System.out.println("输入有误，请重新输入");
				}
			}
		}
	}

	/**
	 * 对工作空间文件内容排序
	 */
	public static void sortWorkspaceFile(String filePath)
	{
		if (StrUtil.isBlank(filePath))
		{
			System.out.println("路径不能为空");
			return;
		}
		File workspaceFile = FileUtil.file(filePath);

		if (!FileUtil.exist(workspaceFile))
		{
			System.out.println("文件不存在");
			return;
		}
		if (!FileUtil.isFile(workspaceFile))
		{
			System.out.println("这个路径是一个目录");
			return;
		}
		if ( !StrUtil.equals("code-workspace", FileUtil.getType(workspaceFile)) )
		{
			System.out.printf("这不是一个vscode工作区文件");
			return;
		}

		FileReader readerUtil = new FileReader(workspaceFile);

		JSONObject content = JSON.parseObject(readerUtil.readString());
		JSONArray folders = content.getJSONArray("folders");
		List<JSONObject> foldersList = folders.toJavaList(JSONObject.class);

		List<JSONObject> firstIsNotChinese = foldersList.stream()
														.filter(o -> !isChineseWord(getPathResource(o.getString("path")).charAt(0)))
														.sorted
														(
															(o1, o2) ->
															{
																String path1 = getPathResource(o1.getString("path"));
																String path2 = getPathResource(o2.getString("path"));

																return StrUtil.compareIgnoreCase(path1, path2, true);
															}
														)
														.collect(Collectors.toList());

		List<JSONObject> firstIsChinese = foldersList.stream()
													.filter(o -> isChineseWord(getPathResource(o.getString("path")).charAt(0)))
													.sorted
													(
														(o1, o2) ->
														{
															String path1 = getNoChineseStr(getPathResource(o1.getString("path")));
															String path2 = getNoChineseStr(getPathResource(o2.getString("path")));

															return StrUtil.compareIgnoreCase(path1, path2, true);
														}
													)
													.collect(Collectors.toList());


		CollUtil.addAll(firstIsNotChinese, firstIsChinese); // 排序后将 中文的排序添加到 英文的list中
		content.remove("folders");
		content.put("folders", firstIsNotChinese);

		String jsonResult = JSONObject.toJSONString(content, SerializerFeature.PrettyFormat);

		// 清除原先的内容，写入排序后的内容
		FileWriter writerUtil = new FileWriter(workspaceFile);
		writerUtil.write(jsonResult);
		System.out.println("排序完成");
	}

	/**
	 * 获得路径最后一个 / 后边的字符串
	 */
	public static String getPathResource(String path)
	{
		return path.substring( path.lastIndexOf('/') + 1 );
	}

	/**
	 * 字符串  ---->  不含汉字的字符串
	 * 汉字会被转换成大写的拼音
	 */
	public static String getNoChineseStr(String str)
	{
		StringBuilder result = new StringBuilder();
		for (int i = 0; i <= str.length()-1; i++)
		{
			char ch = str.charAt(i);

			if (isChineseWord(ch))
				result.append(getPinyin(ch));
			else
				result.append(ch);
		}

		return result.toString();
	}

	/**
	 * 一个汉字 --->  拼音字符串
	 * 如果不是汉字，原样返回
	 * 多音字只返回一个，不加音调
	 */
	public static String getPinyin(char word)
	{
		try
		{
			if (!isChineseWord(word)) // 不是汉字
				return String.valueOf(word);

			HanyuPinyinOutputFormat format = new HanyuPinyinOutputFormat();
			format.setCaseType(HanyuPinyinCaseType.UPPERCASE);//输出大写
			format.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
			format.setVCharType(HanyuPinyinVCharType.WITH_V);

			String[] pinyin = PinyinHelper.toHanyuPinyinStringArray(word, format);

			return pinyin[0];
		}
		catch (BadHanyuPinyinOutputFormatCombination e)
		{
			System.out.println("发生异常：" + e.getMessage());
			return "A";
		}
	}

	/**
	 * 判断这个字符是不是汉字
	 * @return true-是  false-不是
	 */
	public static boolean isChineseWord(char ch)
	{
		return String.valueOf(ch).matches("[\u4e00-\u9fa5]");
	}
}