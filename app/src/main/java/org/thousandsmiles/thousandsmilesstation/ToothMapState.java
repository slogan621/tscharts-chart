/*
 * (C) Copyright Syd Logan 2019
 * (C) Copyright Thousand Smiles Foundation 2019
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
 *
 *  Code derived from https://stackoverflow.com/questions/16968412/how-to-use-flood-fill-algorithm-in-android
 */

package org.thousandsmiles.thousandsmilesstation;

public class ToothMapState {
    private long m_selected = 0L;

    public void addSelected(int tooth) {
        m_selected |= 1 << (tooth - 1);
    }

    public void set(long mask) {
        m_selected = mask;
    }

    public void clear() {
        m_selected = 0L;
    }

    public void clearSelected(int tooth) {
        m_selected &= ~(1 << (tooth - 1));
    }

    public long getSelected() {
        return m_selected;
    }

    public boolean isSelected(int tooth) {
        boolean ret = false;

        if ((m_selected & (1 << (tooth - 1))) == 1 << (tooth - 1)) {
            ret = true;
        }
        return ret;
    }
}
