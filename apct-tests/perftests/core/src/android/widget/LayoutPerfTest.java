/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package android.widget;

import android.app.Activity;
import android.perftests.utils.BenchmarkState;
import android.perftests.utils.PerfStatusReporter;
import android.perftests.utils.StubActivity;
import android.support.test.annotation.UiThreadTest;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.view.View;
import android.view.ViewGroup;

import com.android.core.frameworks.perftests.R;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static android.perftests.utils.LayoutUtils.gatherViewTree;
import static android.perftests.utils.LayoutUtils.requestLayoutForAllNodes;
import static android.view.View.MeasureSpec.AT_MOST;
import static android.view.View.MeasureSpec.EXACTLY;
import static android.view.View.MeasureSpec.UNSPECIFIED;

@LargeTest
@RunWith(Parameterized.class)
public class LayoutPerfTest {
    @Parameterized.Parameters(name = "{0}")
    public static Collection measureSpecs() {
        return Arrays.asList(new Object[][] {
                { "relative", R.layout.test_relative_layout, R.id.relative_layout_root },
                { "linear", R.layout.test_linear_layout, R.id.linear_layout_root },
                { "linear_weighted", R.layout.test_linear_layout_weighted,
                        R.id.linear_layout_weighted_root },
        });
    }

    private int[] mMeasureSpecs = {EXACTLY, AT_MOST, UNSPECIFIED};

    private int mLayoutId;
    private int mViewId;

    public LayoutPerfTest(String key, int layoutId, int viewId) {
        // key is used in the final report automatically.
        mLayoutId = layoutId;
        mViewId = viewId;
    }

    @Rule
    public ActivityTestRule<StubActivity> mActivityRule =
            new ActivityTestRule(StubActivity.class);

    @Rule
    public PerfStatusReporter mPerfStatusReporter = new PerfStatusReporter();

    @Test
    @UiThreadTest
    public void testLayoutPerf() {
        Activity activity = mActivityRule.getActivity();
        activity.setContentView(mLayoutId);

        ViewGroup viewGroup = (ViewGroup) activity.findViewById(mViewId);

        List<View> allNodes = gatherViewTree(viewGroup);
        BenchmarkState state = mPerfStatusReporter.getBenchmarkState();

        int length = mMeasureSpecs.length;
        while (state.keepRunning()) {
            for (int i = 0; i < length; i++) {
                // The overhead of this call is ignorable, like within 1% difference.
                requestLayoutForAllNodes(allNodes);

                viewGroup.measure(mMeasureSpecs[i % length], mMeasureSpecs[i % length]);
                viewGroup.layout(0, 0, viewGroup.getMeasuredWidth(), viewGroup.getMeasuredHeight());
            }
        }
    }
}
