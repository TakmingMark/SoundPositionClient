/**
 * Copyright 2014  XCL-Charts
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 	
 * @Project XCL-Charts 
 * @Description Android图表基类库
 * @author XiongChuanLiang<br/>(xcl_168@aliyun.com)
 * @Copyright Copyright (c) 2014 XCL-Charts (www.xclcharts.com)
 * @license http://www.apache.org/licenses/  Apache v2 License
 * @version 1.5
 */
package com.example.mark.XclChart;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;

import org.xclcharts.chart.ScatterChart;
import org.xclcharts.chart.ScatterData;
import org.xclcharts.common.IFormatterTextCallBack;
import org.xclcharts.event.click.PointPosition;
import org.xclcharts.renderer.XChart;
import org.xclcharts.renderer.XEnum;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;


/**
 * @ClassName ScatterChart01View
 * @Description  散点图例子
 * @author XiongChuanLiang<br/>(xcl_168@aliyun.com)
 */

public class ScatterChart01View extends DemoView {

	private String TAG = "ScatterChart01View";
	private ScatterChart chart = new ScatterChart();
	//分类轴标签集合
	private LinkedList<String> labels = new LinkedList<String>();
	private List<ScatterData> chartData = new LinkedList<ScatterData>();
	
	private Paint mPaintTooltips = new Paint(Paint.ANTI_ALIAS_FLAG); //消除鋸齒

	ScatterData dataSeries1,dataSeries2,dataSeries3,dataSeries4,dataSeries5;
	LinkedHashMap<Double, Double> NodePoint1,NodePoint2,NodePoint3,NodePoint4,NodePoint5;
	public ScatterChart01View(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		initView();
	}
	
	public ScatterChart01View(Context context, AttributeSet attrs){
        super(context, attrs);   
        initView();
	 }
	 
	 public ScatterChart01View(Context context, AttributeSet attrs, int defStyle) {
			super(context, attrs, defStyle);
			initView();
	 }
	 
	 private void initView()
	 {
			chartLabels();
			chartDataSet();
			chartRender();

	 }
	 
	 
	@Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {  
        super.onSizeChanged(w, h, oldw, oldh);  
       //图所占范围大小
		chart.setChartRange(0, 0, 700, 600);
    }  				
	
	
	private void chartRender()
	{
		try {
						
			//设置绘图区默认缩进px值,留置空间显示Axis,Axistitle....		
			int [] ltrb = getBarLnDefaultSpadding();
			chart.setPadding(ltrb[0], ltrb[1], ltrb[2], ltrb[3]);
			
			
			//数据源	
			chart.setCategories(labels);
			chart.setDataSource(chartData);
						
			//坐标系
			//数据轴最大值
			chart.getDataAxis().setAxisMax(100);
			//chart.getDataAxis().setAxisMin(0);
			//数据轴刻度间隔
			chart.getDataAxis().setAxisSteps(10);
			
			//标签轴最大值
			chart.setCategoryAxisMax(100);	
			//标签轴最小值
			chart.setCategoryAxisMin(0);	
			chart.getDataAxis().setHorizontalTickAlign(Align.LEFT);
			chart.getDataAxis().getTickLabelPaint().setTextAlign(Align.CENTER);
			
			//chart.getDataAxis().setDetailModeSteps(4);
			
			
			chart.getDataAxis().getAxisPaint().setColor(Color.rgb(127, 204, 204));
			chart.getCategoryAxis().getAxisPaint().setColor(Color.rgb(127, 204, 204));
			
			chart.getDataAxis().getTickMarksPaint().setColor(Color.rgb(127, 204, 204));
			chart.getCategoryAxis().getTickMarksPaint().setColor(Color.rgb(127, 204, 204));
			
			
			//定义交叉点标签显示格式,特别备注,因曲线图的特殊性，所以返回格式为:  x值,y值
			//请自行分析定制
			chart.setDotLabelFormatter(new IFormatterTextCallBack()
			{

				@Override
				public String textFormatter(String value)
				{
					// TODO Auto-generated method stub						
					String label = "(" + value + ")";
					return (label);
				}
				
			});
			//标题
			chart.setTitle("座標圖");
			//激活点击监听
			chart.ActiveListenItemClick();
			//为了让触发更灵敏，可以扩大5px的点击监听范围
			chart.extPointClickRange(5);
			
			chart.getPointPaint().setStrokeWidth(6);
			
//			//显示十字交叉线
//			chart.showDyLine();
//			chart.getDyLine().setDyLineStyle(XEnum.DyLineStyle.BackwardDiagonal);
						
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.e(TAG, e.toString());
		}
	}
	private void chartDataSet()
	{
		//线1的数据集
		NodePoint1 = new LinkedHashMap<Double, Double>();

		dataSeries1 = new ScatterData("Target Node",NodePoint1,
				(int) Color.rgb(255, 0, 0),XEnum.DotStyle.DOT );
		dataSeries1.setLabelVisible(true);	
		dataSeries1.getDotLabelPaint().setColor((int) Color.rgb(255, 0, 0));
		
		//线2的数据集
		NodePoint2 = new LinkedHashMap<Double, Double>();

		dataSeries2 = new ScatterData("Node1",NodePoint2,
				(int) Color.rgb(54, 141, 238),XEnum.DotStyle.DOT );
		dataSeries2.setLabelVisible(true);	
		dataSeries2.getDotLabelPaint().setColor((int) Color.rgb(191, 79, 75));
		
		
		//线3的数据集
		NodePoint3 = new LinkedHashMap<Double, Double>();

		dataSeries3 = new ScatterData("Node2",NodePoint3,
				(int) Color.rgb(54, 141, 238),XEnum.DotStyle.DOT );
		dataSeries3.setLabelVisible(true);	
		dataSeries3.getDotLabelPaint().setColor((int) Color.rgb(191, 79, 75));

		//线4的数据集
		NodePoint4 = new LinkedHashMap<Double, Double>();

		dataSeries4 = new ScatterData("Node3",NodePoint4,
				(int) Color.rgb(54, 141, 238),XEnum.DotStyle.DOT );
		dataSeries4.setLabelVisible(true);
		dataSeries4.getDotLabelPaint().setColor((int) Color.rgb(191, 79, 75));

		//线4的数据集
		NodePoint5 = new LinkedHashMap<Double, Double>();

		dataSeries5 = new ScatterData("Node4",NodePoint5,
				(int) Color.rgb(54, 141, 238),XEnum.DotStyle.DOT );
		dataSeries5.setLabelVisible(true);
		dataSeries5.getDotLabelPaint().setColor((int) Color.rgb(191, 79, 75));
		
		chartData.add(dataSeries1);				
		chartData.add(dataSeries2);	
		chartData.add(dataSeries3);
		chartData.add(dataSeries4);
		chartData.add(dataSeries5);
	}

	public void remove()
	{
		chartData.remove(dataSeries2);
	}

	public void insertDataSeries(int nodeNumber,double nodeX,double nodeY)
	{
		if(nodeNumber==0)
		{
			NodePoint1.clear();
			NodePoint1.put(nodeX,nodeY);
		}
		else if(nodeNumber==1)
		{
			NodePoint2.put(nodeX,nodeY);
		}
		else if(nodeNumber==2)
		{
			NodePoint3.put(nodeX,nodeY);
		}else if(nodeNumber==3)
		{
			NodePoint4.put(nodeX,nodeY);
		}else if(nodeNumber==4)
		{
			NodePoint5.put(nodeX,nodeY);
		}
	}
	private void chartLabels()
	{
		labels.add("0");
		labels.add("10");
		labels.add("20");
		labels.add("30");
		labels.add("40");
		labels.add("50");
		labels.add("60");
		labels.add("70");
		labels.add("80");
		labels.add("90");
		labels.add("100");
	}

	@Override
    public void render(Canvas canvas) {
        try{
            chart.render(canvas);
        } catch (Exception e){
        	Log.e(TAG, e.toString());
        }
    }

	@Override
	public List<XChart> bindChart() {
		// TODO Auto-generated method stub		
		List<XChart> lst = new ArrayList<XChart>();
		lst.add(chart);		
		return lst;
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// TODO Auto-generated method stub		
		
		super.onTouchEvent(event);
				
		if(event.getAction() == MotionEvent.ACTION_UP)
		{			
			triggerClick(event.getX(),event.getY());	
		}
		return true;
	}
	
	
	//触发监听
	private void triggerClick(float x,float y)
	{
		//交叉线
		if(chart.getDyLineVisible())chart.getDyLine().setCenterXY(x,y);		
		if(!chart.getListenItemClickStatus())
		{
			if(chart.getDyLineVisible()&&chart.getDyLine().isInvalidate())this.invalidate();
		}else{	
				PointPosition record = chart.getPositionRecord(x,y);			
				if( null == record) return;
		
				ScatterData lData = chartData.get(record.getDataID());
				LinkedHashMap<Double, Double> linePoint =  lData.getDataSet();
				int pos = record.getDataChildID();
				int i = 0;
				Iterator it = linePoint.entrySet().iterator();
				while(it.hasNext())
				{
					Entry entry=(Entry)it.next();
					
					if(pos == i)
					{							 						
					     Double xValue =(Double) entry.getKey();
					     Double yValue =(Double) entry.getValue();
					     
					     /*
					     Toast.makeText(this.getContext(), 
									record.getPointInfo() +
									" Key:"+lData.getKey() +								
									" Current Value(key,value):"+
									Double.toString(xValue)+","+Double.toString(yValue), 
									Toast.LENGTH_SHORT).show();
					     */
					     
					   //在点击处显示tooltip
						mPaintTooltips.setColor(Color.RED);
						chart.getToolTip().setCurrentXY(x,y);
						chart.getToolTip().addToolTip(" Key:"+lData.getKey(),mPaintTooltips);		
						chart.getToolTip().addToolTip(
								" Current Value:" + Double.toString(xValue)+","+ Double.toString(yValue),mPaintTooltips);
						this.invalidate();	
					     break;
					}
			        i++;
				}//end while
		} //end if		
	}
	
}
