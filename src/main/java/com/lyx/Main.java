package com.lyx;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.file.FileReader;
import cn.hutool.core.io.file.FileWriter;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.stream.Collectors;

public class Main
{
	public static void main(String[] args)
	{
		String workspaceFilePath = null;

		System.out.print("输入vscode工作区文件路径：");
		try
		(
			// 读取 System.in 即从键盘上输入 最终转换成一个 BufferReader对象
			InputStreamReader inputStreamReader = new InputStreamReader(System.in);
			BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
		)
		{
			// 读入字符串
			workspaceFilePath = bufferedReader.readLine();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

		sortWorkspaceFile(workspaceFilePath);

		System.out.println("排序完成！");
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
		FileWriter writerUtil = new FileWriter(workspaceFile);

		JSONObject content = JSON.parseObject(readerUtil.readString());
		JSONArray folders = content.getJSONArray("folders");
		List<JSONObject> foldersList = folders.toJavaList(JSONObject.class);

		List<JSONObject> foldersResult = foldersList.stream()
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
		content.remove("folders");
		content.put("folders", foldersResult);

		String jsonResult = JSONObject.toJSONString(content, SerializerFeature.PrettyFormat);

		// 清除原先的内容，写入排序后的内容
		writerUtil.write(jsonResult);
	}

	/**
	 * 获得路径最后一个 / 后边的字符串
	 */
	public static String getPathResource(String path)
	{
		return path.substring( path.lastIndexOf('/') + 1 );
	}
}