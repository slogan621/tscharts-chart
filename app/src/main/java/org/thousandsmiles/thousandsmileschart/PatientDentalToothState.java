/*
 * (C) Copyright Syd Logan 2020
 * (C) Copyright Thousand Smiles Foundation 2020
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.thousandsmiles.thousandsmileschart;

import org.thousandsmiles.tscharts_lib.CDTCodesModel;
import org.thousandsmiles.tscharts_lib.CommonSessionSingleton;
import org.thousandsmiles.tscharts_lib.DentalState;

import java.util.ArrayList;

import static org.thousandsmiles.tscharts_lib.DentalState.Location.DENTAL_LOCATION_BOTTOM;
import static org.thousandsmiles.tscharts_lib.DentalState.Location.DENTAL_LOCATION_TOP;
import static org.thousandsmiles.tscharts_lib.DentalState.State.DENTAL_STATE_MISSING;
import static org.thousandsmiles.tscharts_lib.DentalState.State.DENTAL_STATE_TREATED;
import static org.thousandsmiles.tscharts_lib.DentalState.State.DENTAL_STATE_UNTREATED;
import static org.thousandsmiles.tscharts_lib.DentalState.Location.DENTAL_LOCATION_TOP;

public class PatientDentalToothState {
    private int m_id = 0;
    private String m_toothString;
    private int m_toothNumber;
    private boolean m_upper;
    private ArrayList<DentalState.Surface> m_surfaces = new ArrayList<DentalState.Surface>();
    private boolean m_missing;
    private boolean m_completed;
    private CDTCodesModel m_cdtCodesModel;

    public PatientDentalToothState() {
    }

    public PatientDentalToothState(PatientDentalToothState rhs) {
        this.m_id = rhs.m_id;
        this.m_toothString = rhs.m_toothString;
        this.m_toothNumber = rhs.m_toothNumber;
        this.m_upper = rhs.m_upper;
        this.m_surfaces = rhs.m_surfaces;
        this.m_missing = rhs.m_missing;
        this.m_completed = rhs.m_completed;
        this.m_cdtCodesModel = rhs.m_cdtCodesModel;
    }

    public PatientDentalToothState fromDentalState(DentalState state) {
        setCompleted(state.getState() == DENTAL_STATE_TREATED);
        setMissing(state.getState() == DENTAL_STATE_MISSING);
        CommonSessionSingleton.getInstance().getCDTCodesList();

        setCDTCodesModel(CommonSessionSingleton.getInstance().getCDTCodeModelList().getModel(state.getCode()));
        setSurfaces(state.getSurfaces());
        setToothNumber(state.getTooth());
        boolean top = (state.getLocation() == DENTAL_LOCATION_TOP);
        setToothString(AppDentalTreatmentFragment.toothToString(top, m_toothNumber));
        setUpper(top);
        setId(state.getId());
        return this;
    }

    public DentalState toDentalState(int clinic, int patient) {
        DentalState state = new DentalState();
        state.setCode(m_cdtCodesModel.getId());
        state.setClinic(clinic);
        state.setId(m_id);
        state.setPatient(patient);
        if (m_completed) {
            state.setState(DENTAL_STATE_TREATED);
        } else {
            state.setState(DENTAL_STATE_UNTREATED);
        }
        if (m_missing) {
            state.setState(DENTAL_STATE_MISSING);
        }
        state.setTooth(m_toothNumber);
        state.setLocation(m_upper == true ? DENTAL_LOCATION_TOP : DENTAL_LOCATION_BOTTOM);
        if (m_surfaces == null) {
            m_surfaces = new ArrayList<DentalState.Surface>();
        }
        if (m_surfaces.size() == 0) {
            state.addSurface(DentalState.Surface.DENTAL_SURFACE_NONE);
        } else {
            for (int i = 0; i < m_surfaces.size(); i++) {
                DentalState.Surface surface = m_surfaces.get(i);
                state.addSurface(surface);
            }
        }
        state.setComment("");
        state.setUsername("nobody");
        return state;
    }

    public PatientDentalToothState find(ArrayList<PatientDentalToothState> list) {
        PatientDentalToothState ret = null;
        for (int i = 0; i < list.size(); i++) {
            PatientDentalToothState tmp = list.get(i);
            if (tmp.getToothNumber() == getToothNumber() && tmp.getCDTCodesModel().getId() == getCDTCodesModel().getId()) {
                ret = tmp;
                break;
            }
        }
        return ret;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PatientDentalToothState that = (PatientDentalToothState) o;
        boolean equal = m_toothString.equals(that.m_toothString) &&
                m_toothNumber == that.m_toothNumber &&
                m_missing == that.m_missing &&
                m_upper == that.m_upper &&
                m_completed == that.m_completed &&
                m_cdtCodesModel.equals(that.m_cdtCodesModel) &&
                m_surfaces.equals(that.m_surfaces);
        return equal;
    }

    private void setId(int id) {
        m_id = id;
    }

    public int getId() {
        return m_id;
    }

    private void setToothString(String tooth) {
        m_toothString = tooth;
    }

    public String getToothString() {
        return m_toothString;
    }

    public void setToothNumber(int tooth) {
        m_toothNumber = tooth;
        setToothString(AppDentalTreatmentFragment.toothToString(m_upper, m_toothNumber));
    }

    public int getToothNumber() {
        return m_toothNumber;
    }

    public void setUpper(boolean val) {
        m_upper = val;
    }

    public boolean getUpper() {
        return m_upper;
    }

    public void setMissing(boolean missing) {
        m_missing = missing;
    }

    public boolean getMissing() {
        return m_missing;
    }

    public void setCompleted(boolean completed) {
        m_completed = completed;
    }

    public boolean getCompleted() {
        return m_completed;
    }

    public void addSurface(DentalState.Surface surface) {
        m_surfaces.add(surface);
    }

    public void removeSurface(DentalState.Surface surface) {
        m_surfaces.remove(surface);
    }

    public void setSurfaces(ArrayList<DentalState.Surface> list) {
        m_surfaces = list;
    }

    public String getSurfacesString() {
        return new String("");
    }

    public ArrayList<DentalState.Surface> getSurfacesList() {
        return m_surfaces;
    }

    public void setCDTCodesModel(CDTCodesModel model) {
        m_cdtCodesModel = model;
    }

    public CDTCodesModel getCDTCodesModel() {
        return m_cdtCodesModel;
    }
}
