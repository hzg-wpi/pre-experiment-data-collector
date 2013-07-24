/*
 * The main contributor to this project is Institute of Materials Research,
 * Helmholtz-Zentrum Geesthacht,
 * Germany.
 *
 * This project is a contribution of the Helmholtz Association Centres and
 * Technische Universitaet Muenchen to the ESS Design Update Phase.
 *
 * The project's funding reference is FKZ05E11CG1.
 *
 * Copyright (c) 2012. Institute of Materials Research,
 * Helmholtz-Zentrum Geesthacht,
 * Germany.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
 */

//+============================================================================
// $Source:  $
//
// project :     Tango Device Server
//
// Description:	java source code for the wpn.hdri.frontend.JsonDS class and its commands.
//              This class is derived from DeviceImpl class.
//              It represents the CORBA servant obbject which
//              will be accessed from the network. All commands which
//              can be executed on the wpn.hdri.frontend.JsonDS are implemented
//              in this file.
//
// $Author:  $
//
// $Revision:  $
//
// $Log:  $
//
// copyleft :   European Synchrotron Radiation Facility
//              BP 220, Grenoble 38043
//              FRANCE
//
//-============================================================================
//
//  		This file is generated by POGO
//	(Program Obviously used to Generate tango Object)
//
//         (c) - Software Engineering Group - ESRF
//=============================================================================


package JsonDS;


import com.google.common.base.Preconditions;
import fr.esrf.Tango.DevFailed;
import fr.esrf.Tango.DevState;
import fr.esrf.TangoDs.*;
import net.jcip.annotations.NotThreadSafe;
import org.apache.commons.beanutils.DynaBean;
import org.apache.log4j.Logger;
import wpn.hdri.tango.attribute.EnumAttrWriteType;
import wpn.hdri.tango.attribute.TangoAttribute;
import wpn.hdri.tango.attribute.TangoAttributeListener;
import wpn.hdri.tango.data.format.TangoDataFormat;
import wpn.hdri.tango.data.type.TangoDataType;
import wpn.hdri.tango.data.type.TangoDataTypes;
import wpn.hdri.tango.util.TangoUtils;
import wpn.hdri.web.ApplicationContext;
import wpn.hdri.web.data.DataSet;
import wpn.hdri.web.data.DataSets;
import wpn.hdri.web.data.User;
import wpn.hdri.web.data.Users;
import wpn.hdri.web.frontend.TangoServlet;
import wpn.hdri.web.meta.MetaData;
import wpn.hdri.web.meta.MetaDataHelpers;
import wpn.hdri.web.meta.MetaField;
import wpn.hdri.web.storage.Storage;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

/**
 * Class Description:
 *
 * @author $Author: $
 * @version $Revision: $
 */

//--------- Start of States Description ----------
/*
 *	Device States Description:
 */
//--------- End of States Description ----------

@NotThreadSafe
public class JsonDS extends DeviceImpl implements TangoConst {
    private static Logger logger = Logger.getLogger("wpn.hdri.web.frontend." + JsonDS.class.getSimpleName());
    //--------- Start of attributes data members ----------

    //--------- End of attributes data members ----------


    //--------- Start of properties data members ----------
    //--------- End of properties data members ----------


    //	Add your own data members here
    //--------------------------------------
    private DataSet dataSet = null;

    private Storage<DynaBean> storage;
    private ApplicationContext context;

    private final Map<String, TangoAttribute<?>> attributes = new HashMap<String, TangoAttribute<?>>();

    //=========================================================

    /**
     * Constructor for simulated Time Device Server.
     *
     * @param cl The DeviceClass object
     * @param s  The Device name.
     */
//=========================================================
    JsonDS(DeviceClass cl, String s) throws DevFailed {
        super(cl, s);
        init_device();
    }
//=========================================================

    /**
     * Constructor for simulated Time Device Server.
     *
     * @param cl The DeviceClass object
     * @param s  The Device name.
     * @param d  Device description.
     */
//=========================================================
    JsonDS(DeviceClass cl, String s, String d) throws DevFailed {
        super(cl, s, d);
        init_device();
    }


//=========================================================

    /**
     * Initialize the device.
     */
//=========================================================
    public void init_device() throws DevFailed {
        get_logger().info("JsonDS() create " + device_name);

        //	Initialise variables to default values
        //-------------------------------------------
        //TODO avoid this dirty hack with System.properties
        context = (ApplicationContext) System.getProperties().get(TangoServlet.APPLICATION_CONTEXT);

        storage = context.getStorage();

        //add default attributes
        for (JsonDSAttr attr : JsonDSAttr.values()) {
            addAttribute(attr.toTangoAttribute().getName(), attr.toTangoAttribute());
        }

        JsonDSAttr.CRT_BEAMTIME_ID.toTangoAttribute().setCurrentValue(context.getBeamtimeId().getValue());

        //add meta data attributes
        MetaData meta = null;
        try {
            meta = context.getMetaDataHelper().getMetaData();
        } catch (Exception e) {
            throw TangoUtils.createDevFailed(e);
        }
        for (MetaField fld : meta.getAllFields()) {
            final String attrName = fld.getId();
            final JsonDS device = this;
            TangoDataType<Object> type = TangoDataTypes.forClass(MetaDataHelpers.<Object>getFieldTypeAdaptor(fld).getTargetClass());
            TangoAttribute<?> attribute = new TangoAttribute<Object>(
                    attrName, TangoDataFormat.createScalarDataFormat(), type, EnumAttrWriteType.READ_WRITE, new TangoAttributeListener<Object>() {
                @Override
                public Object onLoad() {
                    DataSet dataSet = device.getDataSet();

                    if (MetaDataHelpers.isUpload(attrName, dataSet.getMeta())) {
                        String oldValue = dataSet.get(attrName);
                        return convertFileNames(oldValue);
                    } else {
                        return dataSet.get(attrName);
                    }
                }

                /**
                 * Adds relative path before each file name, i.e. /PreExperimentDataCollector/home/{user_name}/upload/{file_name}.
                 * To get absolute path on server add ${CATALINA_HOME}/webapps before the relative path
                 *
                 * @param oldValue ';' separated file names
                 * @return ';' separated relative paths
                 */
                private String convertFileNames(String oldValue) {
                    StringBuilder bld = new StringBuilder();

                    String[] fileNames = oldValue.split(";");
                    for (String fileName : fileNames) {
                        if (fileName.isEmpty()) continue;
                        bld.append(getRelativeUserUploadDirPath(context, device.getUser())).append('/').append(fileName).append(';');
                    }

                    return bld.toString();
                }

                private StringBuilder getRelativeUserUploadDirPath(ApplicationContext context, User user) {
                    StringBuilder result = new StringBuilder();

                    result.append(context.getUserUploadDirRelativePath(user));

                    return result;
                }

                @Override
                public void onSave(Object value) {
                    DataSet dataSet = device.getDataSet();
                    DataSets.update(attrName, value, dataSet);

                    try {
                        device.getStorage().save(dataSet.getData(), device.getUser(), device.getDataSetName(), context);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            });
            device.addAttribute(attrName, attribute);

        }


        set_state(DevState.RUNNING);
    }
//=========================================================

    /**
     * Method always executed before command execution.
     */
//=========================================================
    public void always_executed_hook() {
        get_logger().info("In always_executed_hook method()");
    }

//===================================================================

    /**
     * Method called by the write_attributes CORBA operation to
     * write device hardware.
     *
     * @param attr_list vector of index in the attribute vector
     *                  of attribute to be written
     */
//===================================================================			
    public void write_attr_hardware(Vector attr_list) throws DevFailed {
        get_logger().info("In write_attr_hardware for " + attr_list.size() + " attribute(s)");

        for (int i = 0; i < attr_list.size(); i++) {
            WAttribute att = dev_attr.get_w_attr_by_ind(((Integer) (attr_list.elementAt(i))).intValue());
            String attr_name = att.get_name();

            TangoAttribute<?> attribute = attributes.get(attr_name);

            attribute.write(att);
        }
    }

//===================================================================

    /**
     * Method called by the read_attributes CORBA operation to
     * read device hardware
     *
     * @param attr_list Vector of index in the attribute vector
     *                  of attribute to be read
     */
//===================================================================			
    public void read_attr_hardware(Vector attr_list) throws DevFailed {
        get_logger().info("In read_attr_hardware for " + attr_list.size() + " attribute(s)");

        //	Switch on attribute name
        //---------------------------------
    }
//===================================================================

    /**
     * Method called by the read_attributes CORBA operation to
     * set internal attribute value.
     *
     * @param attr reference to the Attribute object
     */
//===================================================================			
    public void read_attr(Attribute attr) throws DevFailed {
        String attr_name = attr.get_name();
        get_logger().info("In read_attr for attribute " + attr_name);

        TangoAttribute<?> attribute = attributes.get(attr_name);

        attribute.read(attr);
    }


    public DataSet getDataSet() {
        Preconditions.checkState(dataSet != null, "DataSet is not set. Please call loadDataSet cmd first.");
        return dataSet;
    }

    /**
     * initialized in {@link this#write_attr_hardware(java.util.Vector)}
     */
    public User getUser() {
        User user = Users.getUser(JsonDSAttr.CRT_USER_NAME.<String>toTangoAttribute().getCurrentValue(), false, context);
        return user;
    }

    public void setUser(User user) {
        JsonDSAttr.CRT_USER_NAME.<String>toTangoAttribute().setCurrentValue(user.getName());
    }

    public String getDataSetName() {
        String dataSetName = JsonDSAttr.CRT_USER_SCAN.<String>toTangoAttribute().getCurrentValue();
        return dataSetName;
    }

    public void setDataSetName(String dataSetName) {
        JsonDSAttr.CRT_USER_SCAN.<String>toTangoAttribute().setCurrentValue(dataSetName);
    }

    public Storage<DynaBean> getStorage() {
        return storage;
    }

    public ApplicationContext getContext() {
        return context;
    }

    @Override
    public Logger get_logger() {
        //route log messages to one common logger
        return logger;
    }

    public void setDataSet(DataSet dataSet) {
        this.dataSet = dataSet;
    }

    public void addAttribute(String attrName, TangoAttribute<?> attribute) throws DevFailed {
        add_attribute(attribute.toAttr());
        attributes.put(attrName, attribute);
    }

    @Override
    public void delete_device() throws DevFailed {

    }
}
//--------------------------------------------------------------------------
/* end of $Source: /cvsroot/tango-cs/tango/tools/pogo/templates/java/DevServ.java,v $ */
