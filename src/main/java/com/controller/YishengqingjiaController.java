
package com.controller;

import java.io.File;
import java.math.BigDecimal;
import java.net.URL;
import java.text.SimpleDateFormat;
import com.alibaba.fastjson.JSONObject;
import java.util.*;
import org.springframework.beans.BeanUtils;
import javax.servlet.http.HttpServletRequest;
import org.springframework.web.context.ContextLoader;
import javax.servlet.ServletContext;
import com.service.TokenService;
import com.utils.*;
import java.lang.reflect.InvocationTargetException;

import com.service.DictionaryService;
import org.apache.commons.lang3.StringUtils;
import com.annotation.IgnoreAuth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.mapper.Wrapper;
import com.entity.*;
import com.entity.view.*;
import com.service.*;
import com.utils.PageUtils;
import com.utils.R;
import com.alibaba.fastjson.*;

/**
 * 医生请假
 * 后端接口
 * @author
 * @email
*/
@RestController
@Controller
@RequestMapping("/yishengqingjia")
public class YishengqingjiaController {
    private static final Logger logger = LoggerFactory.getLogger(YishengqingjiaController.class);

    @Autowired
    private YishengqingjiaService yishengqingjiaService;


    @Autowired
    private TokenService tokenService;
    @Autowired
    private DictionaryService dictionaryService;

    //级联表service
    @Autowired
    private YishengService yishengService;

    @Autowired
    private YonghuService yonghuService;


    /**
    * 后端列表
    */
    @RequestMapping("/page")
    public R page(@RequestParam Map<String, Object> params, HttpServletRequest request){
        logger.debug("page方法:,,Controller:{},,params:{}",this.getClass().getName(),JSONObject.toJSONString(params));
        String role = String.valueOf(request.getSession().getAttribute("role"));
        if(false)
            return R.error(511,"永不会进入");
        else if("患者".equals(role))
            params.put("yonghuId",request.getSession().getAttribute("userId"));
        else if("医生".equals(role))
            params.put("yishengId",request.getSession().getAttribute("userId"));
        if(params.get("orderBy")==null || params.get("orderBy")==""){
            params.put("orderBy","id");
        }
        PageUtils page = yishengqingjiaService.queryPage(params);

        //字典表数据转换
        List<YishengqingjiaView> list =(List<YishengqingjiaView>)page.getList();
        for(YishengqingjiaView c:list){
            //修改对应字典表字段
            dictionaryService.dictionaryConvert(c, request);
        }
        return R.ok().put("data", page);
    }

    /**
    * 后端详情
    */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id, HttpServletRequest request){
        logger.debug("info方法:,,Controller:{},,id:{}",this.getClass().getName(),id);
        YishengqingjiaEntity yishengqingjia = yishengqingjiaService.selectById(id);
        if(yishengqingjia !=null){
            //entity转view
            YishengqingjiaView view = new YishengqingjiaView();
            BeanUtils.copyProperties( yishengqingjia , view );//把实体数据重构到view中

                //级联表
                YishengEntity yisheng = yishengService.selectById(yishengqingjia.getYishengId());
                if(yisheng != null){
                    BeanUtils.copyProperties( yisheng , view ,new String[]{ "id", "createTime", "insertTime", "updateTime"});//把级联的数据添加到view中,并排除id和创建时间字段
                    view.setYishengId(yisheng.getId());
                }
            //修改对应字典表字段
            dictionaryService.dictionaryConvert(view, request);
            return R.ok().put("data", view);
        }else {
            return R.error(511,"查不到数据");
        }

    }

    /**
    * 后端保存
    */
    @RequestMapping("/save")
    public R save(@RequestBody YishengqingjiaEntity yishengqingjia, HttpServletRequest request){
        logger.debug("save方法:,,Controller:{},,yishengqingjia:{}",this.getClass().getName(),yishengqingjia.toString());

        String role = String.valueOf(request.getSession().getAttribute("role"));
        if(false)
            return R.error(511,"永远不会进入");
        else if("医生".equals(role))
            yishengqingjia.setYishengId(Integer.valueOf(String.valueOf(request.getSession().getAttribute("userId"))));

        Wrapper<YishengqingjiaEntity> queryWrapper = new EntityWrapper<YishengqingjiaEntity>()
            .eq("yishengqingjia_name", yishengqingjia.getYishengqingjiaName())
            .eq("yishengqingjia_text", yishengqingjia.getYishengqingjiaText())
            .eq("qingjia_types", yishengqingjia.getQingjiaTypes())
            .eq("yishengqingjia_number", yishengqingjia.getYishengqingjiaNumber())
            .eq("yisheng_id", yishengqingjia.getYishengId())
            .eq("yishengqingjia_yesno_types", yishengqingjia.getYishengqingjiaYesnoTypes())
            .eq("yishengqingjia_yesno_text", yishengqingjia.getYishengqingjiaYesnoText())
            ;

        logger.info("sql语句:"+queryWrapper.getSqlSegment());
        YishengqingjiaEntity yishengqingjiaEntity = yishengqingjiaService.selectOne(queryWrapper);
        if(yishengqingjiaEntity==null){
            yishengqingjia.setYishengqingjiaYesnoTypes(1);
            yishengqingjia.setInsertTime(new Date());
            yishengqingjia.setCreateTime(new Date());
            yishengqingjiaService.insert(yishengqingjia);
            return R.ok();
        }else {
            return R.error(511,"表中有相同数据");
        }
    }

    /**
    * 后端修改
    */
    @RequestMapping("/update")
    public R update(@RequestBody YishengqingjiaEntity yishengqingjia, HttpServletRequest request){
        logger.debug("update方法:,,Controller:{},,yishengqingjia:{}",this.getClass().getName(),yishengqingjia.toString());

        String role = String.valueOf(request.getSession().getAttribute("role"));
//        if(false)
//            return R.error(511,"永远不会进入");
//        else if("医生".equals(role))
//            yishengqingjia.setYishengId(Integer.valueOf(String.valueOf(request.getSession().getAttribute("userId"))));
        //根据字段查询是否有相同数据
        Wrapper<YishengqingjiaEntity> queryWrapper = new EntityWrapper<YishengqingjiaEntity>()
            .notIn("id",yishengqingjia.getId())
            .andNew()
            .eq("yishengqingjia_name", yishengqingjia.getYishengqingjiaName())
            .eq("yishengqingjia_text", yishengqingjia.getYishengqingjiaText())
            .eq("qingjia_types", yishengqingjia.getQingjiaTypes())
            .eq("yishengqingjia_time", yishengqingjia.getYishengqingjiaTime())
            .eq("yishengqingjia_number", yishengqingjia.getYishengqingjiaNumber())
            .eq("yisheng_id", yishengqingjia.getYishengId())
            .eq("yishengqingjia_yesno_types", yishengqingjia.getYishengqingjiaYesnoTypes())
            .eq("yishengqingjia_yesno_text", yishengqingjia.getYishengqingjiaYesnoText())
            .eq("insert_time", yishengqingjia.getInsertTime())
            ;

        logger.info("sql语句:"+queryWrapper.getSqlSegment());
        YishengqingjiaEntity yishengqingjiaEntity = yishengqingjiaService.selectOne(queryWrapper);
        if(yishengqingjiaEntity==null){
            yishengqingjiaService.updateById(yishengqingjia);//根据id更新
            return R.ok();
        }else {
            return R.error(511,"表中有相同数据");
        }
    }


    /**
    * 审核
    */
    @RequestMapping("/shenhe")
    public R shenhe(@RequestBody YishengqingjiaEntity yishengqingjia, HttpServletRequest request){
        logger.debug("shenhe方法:,,Controller:{},,yishengqingjia:{}",this.getClass().getName(),yishengqingjia.toString());

//        if(yishengqingjia.getYishengqingjiaYesnoTypes() == 2){//通过
//            yishengqingjia.setYishengqingjiaTypes();
//        }else if(yishengqingjia.getYishengqingjiaYesnoTypes() == 3){//拒绝
//            yishengqingjia.setYishengqingjiaTypes();
//        }
        yishengqingjiaService.updateById(yishengqingjia);//审核
        return R.ok();
    }

    /**
    * 删除
    */
    @RequestMapping("/delete")
    public R delete(@RequestBody Integer[] ids){
        logger.debug("delete:,,Controller:{},,ids:{}",this.getClass().getName(),ids.toString());
        yishengqingjiaService.deleteBatchIds(Arrays.asList(ids));
        return R.ok();
    }


    /**
     * 批量上传
     */
    @RequestMapping("/batchInsert")
    public R save( String fileName, HttpServletRequest request){
        logger.debug("batchInsert方法:,,Controller:{},,fileName:{}",this.getClass().getName(),fileName);
        Integer yonghuId = Integer.valueOf(String.valueOf(request.getSession().getAttribute("userId")));
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            List<YishengqingjiaEntity> yishengqingjiaList = new ArrayList<>();//上传的东西
            Map<String, List<String>> seachFields= new HashMap<>();//要查询的字段
            Date date = new Date();
            int lastIndexOf = fileName.lastIndexOf(".");
            if(lastIndexOf == -1){
                return R.error(511,"该文件没有后缀");
            }else{
                String suffix = fileName.substring(lastIndexOf);
                if(!".xls".equals(suffix)){
                    return R.error(511,"只支持后缀为xls的excel文件");
                }else{
                    URL resource = this.getClass().getClassLoader().getResource("static/upload/" + fileName);//获取文件路径
                    File file = new File(resource.getFile());
                    if(!file.exists()){
                        return R.error(511,"找不到上传文件，请联系管理员");
                    }else{
                        List<List<String>> dataList = PoiUtil.poiImport(file.getPath());//读取xls文件
                        dataList.remove(0);//删除第一行，因为第一行是提示
                        for(List<String> data:dataList){
                            //循环
                            YishengqingjiaEntity yishengqingjiaEntity = new YishengqingjiaEntity();
//                            yishengqingjiaEntity.setYishengqingjiaName(data.get(0));                    //请假原因 要改的
//                            yishengqingjiaEntity.setYishengqingjiaText(data.get(0));                    //详情 要改的
//                            yishengqingjiaEntity.setQingjiaTypes(Integer.valueOf(data.get(0)));   //请假类型 要改的
//                            yishengqingjiaEntity.setYishengqingjiaTime(sdf.parse(data.get(0)));          //请假时间 要改的
//                            yishengqingjiaEntity.setYishengqingjiaNumber(Integer.valueOf(data.get(0)));   //请假天数 要改的
//                            yishengqingjiaEntity.setYishengId(Integer.valueOf(data.get(0)));   //医生 要改的
//                            yishengqingjiaEntity.setYishengqingjiaYesnoTypes(Integer.valueOf(data.get(0)));   //审核结果 要改的
//                            yishengqingjiaEntity.setYishengqingjiaYesnoText(data.get(0));                    //处理结果 要改的
//                            yishengqingjiaEntity.setInsertTime(date);//时间
//                            yishengqingjiaEntity.setCreateTime(date);//时间
                            yishengqingjiaList.add(yishengqingjiaEntity);


                            //把要查询是否重复的字段放入map中
                        }

                        //查询是否重复
                        yishengqingjiaService.insertBatch(yishengqingjiaList);
                        return R.ok();
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
            return R.error(511,"批量插入数据异常，请联系管理员");
        }
    }






}
