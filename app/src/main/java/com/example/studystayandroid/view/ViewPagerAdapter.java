/*
 * StudyStay © 2024
 *
 * All rights reserved.
 *
 * This software and associated documentation files (the "Software") are owned by StudyStay. Unauthorized copying, distribution, or modification of this Software is strictly prohibited.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this Software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 * StudyStay
 * José María Pozo Hidalgo
 * Email: josemariph7@gmail.com
 *
 *
 */

package com.example.studystayandroid.view;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.studystayandroid.model.User;

/**
 * Adaptador para manejar los fragmentos de "Rented" y "Listed" en el ViewPager.
 */
public class ViewPagerAdapter extends FragmentStateAdapter {

    private User currentUser;

    /**
     * Constructor del adaptador.
     * @param fragmentActivity La actividad que aloja el ViewPager.
     * @param user El usuario actual cuyos datos se mostrarán en los fragmentos.
     */
    public ViewPagerAdapter(@NonNull FragmentActivity fragmentActivity, User user) {
        super(fragmentActivity);
        this.currentUser = user;
    }

    /**
     * Crea un nuevo fragmento basado en la posición.
     * @param position La posición del fragmento en el ViewPager.
     * @return El fragmento correspondiente a la posición dada.
     */
    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return RentedFragment.newInstance(currentUser);
            case 1:
                return ListedFragment.newInstance(currentUser);
            default:
                return RentedFragment.newInstance(currentUser);
        }
    }

    /**
     * Devuelve el número de fragmentos en el ViewPager.
     * @return El número total de fragmentos.
     */
    @Override
    public int getItemCount() {
        return 2;
    }
}
