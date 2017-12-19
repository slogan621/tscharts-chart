/*
 * (C) Copyright Syd Logan 2017
 * (C) Copyright Thousand Smiles Foundation 2017
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

package org.thousandsmiles.thousandsmilesstation;

import org.json.JSONException;
import org.json.JSONObject;

public class MedicalHistory {
    private int m_id;
    private int m_clinic;
    private int m_patient;
    private String m_time;
    private boolean m_coldCoughFever;
    private boolean m_hivaids;
    private boolean m_anemia;
    private boolean m_athsma;
    private boolean m_cancer;
    private boolean m_congenitalHeartDefect;
    private boolean m_congenitalHeartDefectWorkup;
    private boolean m_congenitalHeartDefectPlanForCare;
    private boolean m_diabetes;
    private boolean m_epilepsy;
    private boolean m_bleedingProblems;
    private boolean m_hepititis;
    private boolean m_tuberculosis;
    private boolean m_troubleSpeaking;
    private boolean m_troubleHearing;
    private boolean m_troubleEating;
    private int m_pregnancyDuration;
    private boolean m_pregnancySmoke;
    private boolean m_birthComplications;
    private boolean m_pregnancyComplications;
    private boolean m_motherAlcohol;
    private boolean m_relativeCleft;
    private boolean m_parentsCleft;
    private boolean m_siblingsCleft;
    private String m_meds;
    private String m_allergyMeds;
    private int m_firstCrawl;
    private int m_firstSit;
    private int m_firstWalk;
    private int m_firstWords;
    private int m_birthWeight;
    private boolean m_birthWeightMetric;
    private int m_height;
    private boolean m_heightMetric;
    private int m_weight;
    private boolean m_weightMetric;

    public boolean isBirthWeightMetric() {
        return m_birthWeightMetric;
    }

    public void setBirthWeightMetric(boolean birthWeightMetric) {
        m_birthWeightMetric = birthWeightMetric;
    }

    public boolean isHeightMetric() {
        return m_heightMetric;
    }

    public void setHeightMetric(boolean heightMetric) {
        m_heightMetric = heightMetric;
    }

    public boolean isWeightMetric() {
        return m_weightMetric;
    }

    public void setWeightMetric(boolean weightMetric) {
        m_weightMetric = weightMetric;
    }

    public MedicalHistory() {
    }

    public int getClinic() {
        return m_clinic;
    }

    public void setClinic(int clinic) {
        m_clinic = clinic;
    }

    public int getId() {
        return m_id;
    }

    public void setId(int id) {
        m_id = id;
    }

    public int getPatient() {
        return m_patient;
    }

    public void setPatient(int patient) {
        m_patient = patient;
    }


    public String getTime() {
        return m_time;
    }

    public void setTime(String time) {
        m_time = time;
    }

    public boolean isColdCoughFever() {
        return m_coldCoughFever;
    }

    public void setColdCoughFever(boolean cold_cough_fever) {
        m_coldCoughFever = cold_cough_fever;
    }

    public boolean isHivaids() {
        return m_hivaids;
    }

    public void setHivaids(boolean hivaids) {
        m_hivaids = hivaids;
    }

    public boolean isAnemia() {
        return m_anemia;
    }

    public void setAnemia(boolean anemia) {
        m_anemia = anemia;
    }

    public boolean isAthsma() {
        return m_athsma;
    }

    public void setAthsma(boolean athsma) {
        m_athsma = athsma;
    }

    public boolean isCancer() {
        return m_cancer;
    }

    public void setCancer(boolean cancer) {
        m_cancer = cancer;
    }

    public boolean isCongenitalHeartDefect() {
        return m_congenitalHeartDefect;
    }

    public void setCongenitalHeartDefect(boolean congenitalHeartDefect) {
        m_congenitalHeartDefect = congenitalHeartDefect;
    }

    public boolean isCongenitalHeartDefectWorkup() {
        return m_congenitalHeartDefectWorkup;
    }

    public void setCongenitalHeartDefectWorkup(boolean congenitalHeartDefectWorkup) {
        m_congenitalHeartDefectWorkup = congenitalHeartDefectWorkup;
    }

    public boolean isCongenitalHeartDefectPlanForCare() {
        return m_congenitalHeartDefectPlanForCare;
    }

    public void setCongenitalHeartDefectPlanForCare(boolean congenitalHeartDefectPlanForCare) {
        m_congenitalHeartDefectPlanForCare = congenitalHeartDefectPlanForCare;
    }

    public boolean isDiabetes() {
        return m_diabetes;
    }

    public void setDiabetes(boolean diabetes) {
        m_diabetes = diabetes;
    }

    public boolean isEpilepsy() {
        return m_epilepsy;
    }

    public void setEpilepsy(boolean epilepsy) {
        m_epilepsy = epilepsy;
    }

    public boolean isBleedingProblems() {
        return m_bleedingProblems;
    }

    public void setBleedingProblems(boolean bleedingProblems) {
        m_bleedingProblems = bleedingProblems;
    }

    public boolean isHepititis() {
        return m_hepititis;
    }

    public void setHepititis(boolean hepititis) {
        m_hepititis = hepititis;
    }

    public boolean isTuberculosis() {
        return m_tuberculosis;
    }

    public void setTuberculosis(boolean tuberculosis) {
        m_tuberculosis = tuberculosis;
    }

    public boolean isTroubleSpeaking() {
        return m_troubleSpeaking;
    }

    public void setTroubleSpeaking(boolean troubleSpeaking) {
        m_troubleSpeaking = troubleSpeaking;
    }

    public boolean isTroubleHearing() {
        return m_troubleHearing;
    }

    public void setTroubleHearing(boolean troubleHearing) {
        m_troubleHearing = troubleHearing;
    }

    public boolean isTroubleEating() {
        return m_troubleEating;
    }

    public void setTroubleEating(boolean troubleEating) {
        m_troubleEating = troubleEating;
    }

    public int getPregnancyDuration() {
        return m_pregnancyDuration;
    }

    public void setPregnancyDuration(int pregnancyDuration) {
        m_pregnancyDuration = pregnancyDuration;
    }

    public boolean isPregnancySmoke() {
        return m_pregnancySmoke;
    }

    public void setPregnancySmoke(boolean pregnancySmoke) {
        m_pregnancySmoke = pregnancySmoke;
    }

    public boolean isBirthComplications() {
        return m_birthComplications;
    }

    public void setBirthComplications(boolean birthComplications) {
        m_birthComplications = birthComplications;
    }

    public boolean isPregnancyComplications() {
        return m_pregnancyComplications;
    }

    public void setPregnancyComplications(boolean pregnancyComplications) {
        m_pregnancyComplications = pregnancyComplications;
    }

    public boolean isMotherAlcohol() {
        return m_motherAlcohol;
    }

    public void setMotherAlcohol(boolean motherAlcohol) {
        m_motherAlcohol = motherAlcohol;
    }

    public boolean isRelativeCleft() {
        return m_relativeCleft;
    }

    public void setRelativeCleft(boolean relativeCleft) {
        m_relativeCleft = relativeCleft;
    }

    public boolean isParentsCleft() {
        return m_parentsCleft;
    }

    public void setParentsCleft(boolean parentsCleft) {
        m_parentsCleft = parentsCleft;
    }

    public boolean isSiblingsCleft() {
        return m_siblingsCleft;
    }

    public void setSiblingsCleft(boolean siblingsCleft) {
        m_siblingsCleft = siblingsCleft;
    }

    public String getMeds() {
        return m_meds;
    }

    public void setMeds(String meds) {
        m_meds = meds;
    }

    public String getAllergyMeds() {
        return m_allergyMeds;
    }

    public void setAllergyMeds(String allergyMeds) {
        m_allergyMeds = allergyMeds;
    }

    public int getFirstCrawl() {
        return m_firstCrawl;
    }

    public void setFirstCrawl(int firstCrawl) {
        m_firstCrawl = firstCrawl;
    }

    public int getFirstSit() {
        return m_firstSit;
    }

    public void setFirstSit(int firstSit) {
        m_firstSit = firstSit;
    }

    public int getFirstWalk() {
        return m_firstWalk;
    }

    public void setFirstWalk(int firstWalk) {
        m_firstWalk = firstWalk;
    }

    public int getFirstWords() {
        return m_firstWords;
    }

    public void setFirstWords(int firstWords) {
        m_firstWords = firstWords;
    }

    public int getBirthWeight() {
        return m_birthWeight;
    }

    public void setBirthWeight(int birthWeight) {
        m_birthWeight = birthWeight;
    }

    public int getHeight() {
        return m_height;
    }

    public void setHeight(int height) {
        m_height = height;
    }

    public int getWeight() {
        return m_weight;
    }

    public void setWeight(int weight) {
        m_weight = weight;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!MedicalHistory.class.isAssignableFrom(obj.getClass())) {
            return false;
        }
        final MedicalHistory other = (MedicalHistory) obj;

        if (this.m_coldCoughFever != other.m_coldCoughFever) {
            return false;
        }

        if (this.m_hivaids != other.m_hivaids) {
            return false;
        }

        if (this.m_anemia != other.m_anemia) {
            return false;
        }

        if (this.m_athsma != other.m_athsma) {
            return false;
        }

        if (this.m_cancer != other.m_cancer) {
            return false;
        }

        if (this.m_congenitalHeartDefect != other.m_congenitalHeartDefect) {
            return false;
        }

        if (this.m_congenitalHeartDefectWorkup != other.m_congenitalHeartDefectWorkup) {
            return false;
        }

        if (this.m_congenitalHeartDefectPlanForCare != other.m_congenitalHeartDefectPlanForCare) {
            return false;
        }

        if (this.m_diabetes != other.m_diabetes) {
            return false;
        }

        if (this.m_epilepsy != other.m_epilepsy) {
            return false;
        }

        if (this.m_bleedingProblems != other.m_bleedingProblems) {
            return false;
        }

        if (this.m_hepititis != other.m_hepititis) {
            return false;
        }

        if (this.m_tuberculosis != other.m_tuberculosis) {
            return false;
        }

        if (this.m_troubleSpeaking != other.m_troubleSpeaking) {
            return false;
        }

        if (this.m_troubleHearing != other.m_troubleHearing) {
            return false;
        }

        if (this.m_troubleEating != other.m_troubleEating) {
            return false;
        }

        if (this.m_pregnancyDuration != other.m_pregnancyDuration) {
            return false;
        }

        if (this.m_pregnancySmoke != other.m_pregnancySmoke) {
            return false;
        }

        if (this.m_birthComplications != other.m_birthComplications) {
            return false;
        }

        if (this.m_pregnancyComplications != other.m_pregnancyComplications) {
            return false;
        }

        if (this.m_motherAlcohol != other.m_motherAlcohol) {
            return false;
        }

        if (this.m_relativeCleft != other.m_relativeCleft) {
            return false;
        }

        if (this.m_parentsCleft != other.m_parentsCleft) {
            return false;
        }

        if (this.m_siblingsCleft != other.m_siblingsCleft) {
            return false;
        }

        if ((this.m_meds == null) ? (other.m_meds != null) : !this.m_meds.equals(other.m_meds)) {
            return false;
        }

        if ((this.m_allergyMeds == null) ? (other.m_allergyMeds != null) : !this.m_allergyMeds.equals(other.m_allergyMeds)) {
            return false;
        }

        if (this.m_firstCrawl != other.m_firstCrawl) {
            return false;
        }

        if (this.m_firstSit != other.m_firstSit) {
            return false;
        }

        if (this.m_firstWalk != other.m_firstWalk) {
            return false;
        }

        if (this.m_firstWords != other.m_firstWords) {
            return false;
        }

        if (this.m_birthWeight != other.m_birthWeight) {
            return false;
        }

        if (this.m_birthWeightMetric != other.m_birthWeightMetric) {
            return false;
        }

        if (this.m_height != other.m_height) {
            return false;
        }

        if (this.m_heightMetric != other.m_heightMetric) {
            return false;
        }

        if (this.m_weight != other.m_weight) {
            return false;
        }

        if (this.m_weightMetric != other.m_weightMetric) {
            return false;
        }
        return true;
    }

    public int fromJSONObject(JSONObject o)
    {
        int ret = 0;

        try {
            this.setId(o.getInt("id"));
            this.setAllergyMeds(o.getString("allergymeds"));
            this.setAnemia(o.getBoolean("anemia"));
            this.setAthsma(o.getBoolean("athsma"));
            this.setBirthComplications(o.getBoolean("birth_complications"));
            this.setBirthWeight(o.getInt("birth_weight"));
            this.setBirthWeightMetric(o.getBoolean("birth_weight_metric"));
            this.setBleedingProblems(o.getBoolean("bleeding_problems"));
            this.setCancer(o.getBoolean("cancer"));
            this.setClinic(o.getInt("clinic"));
            this.setCongenitalHeartDefect(o.getBoolean("congenitalheartdefect"));
            this.setCongenitalHeartDefectPlanForCare(o.getBoolean("congenitalheartdefect_planforcare"));
            this.setCongenitalHeartDefectWorkup(o.getBoolean("congenitalheartdefect_workup"));
            this.setColdCoughFever(o.getBoolean("cold_cough_fever"));
            this.setDiabetes(o.getBoolean("diabetes"));
            this.setEpilepsy(o.getBoolean("epilepsy"));
            this.setFirstCrawl(o.getInt("first_crawl"));
            this.setFirstSit(o.getInt("first_sit"));
            this.setFirstWalk(o.getInt("first_walk"));
            this.setFirstWords(o.getInt("first_words"));
            this.setHeight(o.getInt("height"));
            this.setHeightMetric(o.getBoolean("height_metric"));
            this.setHepititis(o.getBoolean("hepititis"));
            this.setHivaids(o.getBoolean("hivaids"));
            this.setMeds(o.getString("meds"));
            this.setMotherAlcohol(o.getBoolean("mother_alcohol"));
            this.setParentsCleft(o.getBoolean("parents_cleft"));
            this.setPatient(o.getInt("patient"));
            this.setPregnancyComplications(o.getBoolean("pregnancy_complications"));
            this.setPregnancyDuration(o.getInt("pregnancy_duration"));
            this.setPregnancySmoke(o.getBoolean("pregnancy_smoke"));
            this.setRelativeCleft(o.getBoolean("relative_cleft"));
            this.setSiblingsCleft(o.getBoolean("siblings_cleft"));
            this.setTroubleEating(o.getBoolean("troubleeating"));
            this.setTroubleHearing(o.getBoolean("troublehearing"));
            this.setTroubleSpeaking(o.getBoolean("troublespeaking"));
            this.setTuberculosis(o.getBoolean("tuberculosis"));
            this.setWeight(o.getInt("weight"));
            this.setWeightMetric(o.getBoolean("weight_metric"));
        } catch (JSONException e) {
            ret = -1;
        }
        return ret;
    }

    public JSONObject toJSONObject()
    {
        JSONObject data = new JSONObject();
        try {
            data.put("id", this.getId());
            data.put("allergymeds", this.getAllergyMeds());
            data.put("anemia", this.isAnemia());
            data.put("athsma", this.isAthsma());
            data.put("birth_complications", this.isBirthComplications());
            data.put("birth_weight", this.getBirthWeight());
            data.put("birth_weight_metric", this.isBirthWeightMetric());
            data.put("bleeding_problems", this.isBleedingProblems());
            data.put("cancer", this.isCancer());
            data.put("clinic", this.getClinic());
            data.put("congenitalheartdefect", this.isCongenitalHeartDefect());
            data.put("congenitalheartdefect_planforcare", this.isCongenitalHeartDefectPlanForCare());
            data.put("congenitalheartdefect_workup", this.isCongenitalHeartDefectWorkup());
            data.put("cold_cough_fever", this.isColdCoughFever());
            data.put("diabetes", this.isDiabetes());
            data.put("epilepsy", this.isEpilepsy());
            data.put("first_crawl", this.getFirstCrawl());
            data.put("first_sit", this.getFirstSit());
            data.put("first_walk", this.getFirstWalk());
            data.put("first_words", this.getFirstWords());
            data.put("height", this.getHeight());
            data.put("height_metric", this.isHeightMetric());
            data.put("hepititis", this.isHepititis());
            data.put("hivaids", this.isHivaids());
            data.put("meds", this.getMeds());
            data.put("mother_alcohol", this.isMotherAlcohol());
            data.put("parents_cleft", this.isParentsCleft());
            data.put("patient", this.getPatient());
            data.put("pregnancy_complications", this.isPregnancyComplications());
            data.put("pregnancy_duration", this.getPregnancyDuration());
            data.put("pregnancy_smoke", this.isPregnancySmoke());
            data.put("relative_cleft", this.isRelativeCleft());
            data.put("siblings_cleft", this.isSiblingsCleft());
            data.put("troubleeating", this.isTroubleEating());
            data.put("troublehearing", this.isTroubleHearing());
            data.put("troublespeaking", this.isTroubleSpeaking());
            data.put("tuberculosis", this.isTuberculosis());
            data.put("weight", this.getWeight());
            data.put("weight_metric", this.isWeightMetric());
        } catch(Exception e) {
            // not sure this would ever happen, ignore. Continue on with the request with the expectation it fails
            // because of the bad JSON sent
            data = null;
        }
        return data;
    }
}
