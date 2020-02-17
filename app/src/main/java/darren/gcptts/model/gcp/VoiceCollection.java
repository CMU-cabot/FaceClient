/*******************************************************************************
 * Copyright (c) 2019  Carnegie Mellon University
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 *******************************************************************************/
package darren.gcptts.model.gcp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Author: Changemyminds.
 * Date: 2018/6/24.
 * Description:
 * Reference:
 */
public class VoiceCollection {
    HashMap<String, List<GCPVoice>> hashMap;

    public VoiceCollection() {
        hashMap = new HashMap<>();
    }

    public void add(String language, GCPVoice gcpVoice) {
        if (hashMap.get(language) == null) {
            List<GCPVoice> list = new ArrayList<>();
            list.add(gcpVoice);
            hashMap.put(language, list);
            return;
        }

        List<GCPVoice> list = hashMap.get(language);
        list.add(gcpVoice);
    }

    public String[] getLanguage() {
        if (hashMap.size() == 0) {
            return null;
        }

        List<String> languages = new ArrayList<>();
        languages.addAll(hashMap.keySet());
        return languages.toArray(new String[languages.size()]);
    }

    public String[] getNames(String language) {
        if (language == null || language.length() == 0 || hashMap.get(language) == null) {
            return null;
        }

        List<String> names = new ArrayList<>();
        List<GCPVoice> gcpVoices = hashMap.get(language);
        for (GCPVoice gcpVoice : gcpVoices) {
            names.add(gcpVoice.getName());
        }

        return names.toArray(new String[names.size()]);
    }

    public List<GCPVoice> getGCPVoices(String language) {
        if (hashMap.get(language) == null) {
            return null;
        }

        return hashMap.get(language);
    }

    public GCPVoice getGCPVoice(String language, String name) {
        if (language == null || language.length() == 0 ||
                name == null || name.length() == 0 || hashMap.get(language) == null) {
            return null;
        }

        List<GCPVoice> gcpVoices = hashMap.get(language);
        for (GCPVoice gcpVoice : gcpVoices) {
            if (gcpVoice.getName().compareTo(name) == 0) {
                return gcpVoice;
            }
        }

        return null;
    }

    public void clear() {
        for (HashMap.Entry<String, List<GCPVoice>> entry : hashMap.entrySet()) {
            List<GCPVoice> gcpVoices = entry.getValue();
            gcpVoices.clear();
        }

        hashMap.clear();
    }

    public int size(){
        return hashMap.size();
    }


}
